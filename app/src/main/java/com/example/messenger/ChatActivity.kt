package com.example.messenger

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.adapters.MessageAdapter
import com.example.messenger.databinding.ActivityChatBinding
import com.example.pojo.*
import com.example.sharedPreferences.AppSharedPreferences
import com.example.tools.LoadingProgress
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
import java.util.*


class ChatActivity : AppCompatActivity() {

    lateinit var binding: ActivityChatBinding
    lateinit var context: Context
    private lateinit var appPref: AppSharedPreferences
    lateinit var messageAdapter:MessageAdapter
    lateinit var lm: LinearLayoutManager

    lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    private val fireStoreInstance:FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }
    private val storageInstance: FirebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }

    private val currentUserStorageRef: StorageReference get() = storageInstance.reference.child(FirebaseAuth.getInstance().currentUser!!.uid)

    lateinit var currentChannelId:String

    private val chatChannelsCollectionRef = fireStoreInstance.collection("chatChannels")

    lateinit var currentUserUID:String
    lateinit var currentFriend:User

    lateinit var loadingProgress: LoadingProgress


    lateinit var bottomSheet:CoordinatorLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadingProgress = LoadingProgress(this)

        context = this

        appPref = AppSharedPreferences()

        appPref.PrefManager(this)

        currentUserUID = appPref.getCurrentUserUID()

        createBottomSheet()

        initializingRecyclerView()

        val bundle = intent.extras

        currentFriend = bundle?.get("person") as User

        binding.imageviewPhotoProfile.setOnClickListener {
            val intent = Intent(this ,PublicProfileActivity::class.java)
            intent.putExtra("person" ,currentFriend)
            startActivity(intent)
        }

        createChatChannel()
        {
            channelId ->
            currentChannelId = channelId
            getMessageFromFireBase(channelId)
            binding.buSendMessage.setOnClickListener {
            if (binding.edittextSendMessage.text.isNotBlank() && binding.edittextSendMessage.text.isNotEmpty())
            {
                sendMessage(channelId,TextMessage(binding.edittextSendMessage.text.toString() ,currentUserUID ,currentFriend.uid ,appPref.getCurrentUserName() ,currentFriend.name ,Calendar.getInstance().time))
                binding.edittextSendMessage.setText("")
            }

            }
        }
        bottombarSendMessageAnimation()

        buChatTollBarSelected()

        binding.tvUsername.text = currentFriend.name
        Glide.with(this).load(currentFriend.imagePath).placeholder(R.drawable.ic_photo_placeholder).into(binding.imageviewPhotoProfile)

        binding.buSendImage.setOnClickListener {
            val intentImage = Intent().apply {
                type = "image/*"
                action = Intent.ACTION_GET_CONTENT
                putExtra(Intent.EXTRA_MIME_TYPES , arrayOf("image/jpeg" ,"image/png"))
            }

            activityResultLauncher.launch(intentImage)
        }

        activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult() ,object :
                ActivityResultCallback<ActivityResult> {
                override fun onActivityResult(result: ActivityResult?) {
                    if (result?.resultCode == Activity.RESULT_OK && result.data != null)
                    {
                        val imagePath = result.data!!.data
                        compressImage(imagePath!!)
                    }
                }
            })

    }

    fun compressImage(imageUri: Uri){
        val outputStream = ByteArrayOutputStream()
        MediaStore.Images.Media.getBitmap(this.contentResolver ,imageUri).compress(Bitmap.CompressFormat.JPEG ,30 ,outputStream)
        upLoadProfileImageToFirebase(outputStream.toByteArray())
        {
                path -> sendMessage(currentChannelId , ImageMessage(path ,currentUserUID ,currentFriend.uid ,appPref.getCurrentUserName() ,currentFriend.name ,Calendar.getInstance().time))
        }
    }

    fun upLoadProfileImageToFirebase(imageByteArray:ByteArray ,onSuccess:(imagePath:String) -> Unit)
    {
        loadingProgress.show()
        val ref = currentUserStorageRef.child("images/${UUID.nameUUIDFromBytes(imageByteArray)}")
        ref.putBytes(imageByteArray).addOnCompleteListener {
            if (it.isSuccessful)
            {
                ref.downloadUrl.addOnCompleteListener {
                        task->
                    if (task.isSuccessful)
                    {
                        onSuccess(task.result.toString())
                        loadingProgress.hide()
                        Toast.makeText(this ,"Uploading Successfully" , Toast.LENGTH_SHORT).show()
                    }
                    else
                    {
                        loadingProgress.hide()
                        Toast.makeText(this , task.exception?.message.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
            }
            else
            {
                loadingProgress.hide()
                Toast.makeText(this , it.exception?.message.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }
    fun initializingRecyclerView()
    {
        lm = LinearLayoutManager(this)

        lm.reverseLayout = true

        messageAdapter = MessageAdapter(currentUserUID)

        binding.rvChat.adapter = messageAdapter

        binding.rvChat.layoutManager = lm

    }

    fun sendMessage(channelId:String, message:Message)
    {
        chatChannelsCollectionRef.document(channelId).collection("messages").add(message)
    }


    fun createChatChannel(onComplete:(channelId:String) -> Unit)
    {
        val chatChannel = fireStoreInstance.collection("users").document()

        fireStoreInstance.collection("users")
            .document(currentUserUID)
            .collection("sharedChat")
            .document(currentFriend.uid)
            .get().addOnSuccessListener {
                document ->
                if (document.exists()) {
                    onComplete(document["channelId"].toString())
                    return@addOnSuccessListener
                }
        fireStoreInstance.collection("users")
            .document(currentFriend.uid)
            .collection("sharedChat")
            .document(currentUserUID)
            .set(mapOf("channelId" to chatChannel.id))

        fireStoreInstance.collection("users")
            .document(currentUserUID)
            .collection("sharedChat")
            .document(currentFriend.uid)
            .set(mapOf("channelId" to chatChannel.id))
                onComplete(chatChannel.id)
            }
    }
    fun buChatTollBarSelected()
    {
        binding.toolBarBuChat.setTextColor(Color.parseColor("#51BA65"))
        binding.toolBarBuChat.setBackgroundResource(R.drawable.shape_circle)
        binding.toolBarBuFiles.setTextColor(Color.parseColor("#FFFFFFFF"))
        binding.toolBarBuFiles.setBackgroundResource(R.color.my_green)

        binding.toolBarBuChat.setOnClickListener {
            binding.toolBarBuChat.setTextColor(Color.parseColor("#51BA65"))
            binding.toolBarBuChat.setBackgroundResource(R.drawable.shape_circle)
            binding.toolBarBuFiles.setTextColor(Color.parseColor("#FFFFFFFF"))
            binding.toolBarBuFiles.setBackgroundResource(R.color.my_green)
        }

        binding.toolBarBuFiles.setOnClickListener {
            binding.toolBarBuFiles.setTextColor(Color.parseColor("#51BA65"))
            binding.toolBarBuFiles.setBackgroundResource(R.drawable.shape_circle)
            binding.toolBarBuChat.setTextColor(Color.parseColor("#FFFFFFFF"))
            binding.toolBarBuChat.setBackgroundResource(R.color.my_green)
        }
    }


    fun bottombarSendMessageAnimation()
    {

        binding.buSendMessage.visibility = View.INVISIBLE

        val margin = resources.getDimension(R.dimen.text_margin).toInt()
        val layoutParams = CoordinatorLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        binding.edittextSendMessage.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s!!.isNotBlank() && s.isNotEmpty())
                {
                    layoutParams.setMargins(0,0,margin ,0)
                    binding.buSendMessage.visibility = View.VISIBLE
                    binding.edittextSendMessage.layoutParams = layoutParams

                }
                else
                {
                    layoutParams.setMargins(0, 0, 0, 0)
                    binding.buSendMessage.visibility = View.INVISIBLE
                    binding.edittextSendMessage.layoutParams = layoutParams
                }
            }
            override fun afterTextChanged(s: Editable?) {
            }
        })
    }
    fun getMessageFromFireBase(channelId: String)
    {
        val arrayOfReceiveMessage = arrayListOf<ReceiveMessage>()
        val query = chatChannelsCollectionRef.document(channelId).collection("messages").orderBy("date" ,Query.Direction.DESCENDING)
        query.addSnapshotListener { querySnapshot, error ->
            messageAdapter.arrayOfMessages.clear()
            querySnapshot!!.documents.forEach {
                document ->
                if (document["type"] == MessageType.TEXT)
                {
                    arrayOfReceiveMessage.add(ReceiveMessage(document.toObject(TextMessage::class.java)!!,document.id))
                    Log.d("chat" ,ReceiveMessage(document.toObject(TextMessage::class.java)!!,document.id).toString())
                }
                else
                {
                    arrayOfReceiveMessage.add(ReceiveMessage(document.toObject(ImageMessage::class.java)!!,document.id))
                    Log.d("chat" ,ReceiveMessage(document.toObject(TextMessage::class.java)!!,document.id).toString())
                }
            }
            messageAdapter.setList(arrayOfReceiveMessage)

            binding.rvChat.scrollToPosition(0)
        }
    }
    fun createBottomSheet()
    {
        bottomSheet = findViewById(R.id.bottomSheet)
        val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        binding.buMenuBottomSheet.setOnClickListener(View.OnClickListener {
            if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_HIDDEN)
            {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

            }else
            {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            }
        })
    }
}