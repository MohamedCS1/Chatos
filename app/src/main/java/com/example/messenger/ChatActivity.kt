package com.example.messenger

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.adapters.MessageAdapter
import com.example.messenger.databinding.ActivityChatBinding
import com.example.messenger.databinding.BottomSheetLayoutBinding
import com.example.pojo.Message
import com.example.pojo.Person
import com.example.pojo.ReceiveMessage
import com.example.sharedPreferences.AppSharedPreferences
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.*


class ChatActivity : AppCompatActivity() {

    lateinit var binding: ActivityChatBinding
    lateinit var context: Context
    lateinit var appPref: AppSharedPreferences
    lateinit var messageAdapter:MessageAdapter
    lateinit var lm: LinearLayoutManager

    private val fireStoreInstance:FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    private val chatChannelsCollectionRef = fireStoreInstance.collection("chatChannels")

    lateinit var currentUserUID:String
    lateinit var currentFriend:Person

    lateinit var bottomSheet:CoordinatorLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        createBottomSheet()

        lm = LinearLayoutManager(this)

        lm.reverseLayout = true

        context = this

        appPref = AppSharedPreferences()

        appPref.PrefManager(this)

        currentUserUID = appPref.getCurrentUserUID()

        messageAdapter = MessageAdapter(currentUserUID)

        val bundle = intent.extras

        currentFriend = bundle?.get("person") as Person

        createChatChannel()
        {
            channelId ->
            getMessageFromFireBase(channelId)
            binding.buSendMessage.setOnClickListener {
            if (binding.edittextSendMessage.text.isNotBlank() && binding.edittextSendMessage.text.isNotEmpty())
            {
                sendMessage(channelId,Message(binding.edittextSendMessage.text.toString() ,currentUserUID ,currentFriend.uid ,Calendar.getInstance().time))
                binding.edittextSendMessage.setText(" ")
            }

        }


        }
        binding.edittextSendMessage.setOnClickListener {
            Toast.makeText(this , "Any" ,Toast.LENGTH_SHORT).show()
        }
        bottomToolbarSendMessageAnimation()

        buChatTollBarSelected()

        Toast.makeText(this ,currentFriend.uid ,Toast.LENGTH_SHORT).show()

        binding.tvUsername.text = currentFriend.name
        Glide.with(this).load(currentFriend.imagePath).placeholder(R.drawable.ic_photo_placeholder).into(binding.imageviewPhotoProfile)

        binding.rvChat.adapter = messageAdapter
        binding.rvChat.layoutManager = lm
    }

    fun sendMessage(channelId:String ,message:Message)
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


    fun bottomToolbarSendMessageAnimation()
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
                arrayOfReceiveMessage.add(ReceiveMessage(document.toObject(Message::class.java)!!,document.id))
                Log.d("chat" ,ReceiveMessage(document.toObject(Message::class.java)!!,document.id).toString())
            }
            binding.nestedScrollViewChat.fullScroll(View.FOCUS_DOWN)
            messageAdapter.setList(arrayOfReceiveMessage)
        }
    }

    fun createBottomSheet()
    {
        bottomSheet = findViewById(R.id.bottomSheet)
        val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        binding.buMenuBottomSheet.setOnClickListener(View.OnClickListener {
            if (bottomSheetBehavior.state.equals(BottomSheetBehavior.STATE_HIDDEN))
            {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

            }else
            {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            }
        })
    }
}