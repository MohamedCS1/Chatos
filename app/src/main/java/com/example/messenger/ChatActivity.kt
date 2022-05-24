package com.example.messenger

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.provider.MediaStore
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.adapters.ChatImagesAdapter
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
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.*


class ChatActivity : AppCompatActivity() {

    lateinit var binding: ActivityChatBinding
    lateinit var context: Context
    private lateinit var appPref: AppSharedPreferences
    lateinit var messageAdapter:MessageAdapter
    lateinit var chatImagesAdapter:ChatImagesAdapter
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

    var recorder: MediaRecorder? = null

    private var fileName: String = ""

    lateinit var timer:Timer

    @SuppressLint("ClickableViewAccessibility")
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

        fileRecordingInit()



        binding.buRecording.setOnTouchListener{ v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                startRecording()
                Toast.makeText(this ,"start recording" ,Toast.LENGTH_SHORT).show()
            } else if (event.action == MotionEvent.ACTION_UP) {
                stopRecording()
                Toast.makeText(this ,"stop recording" ,Toast.LENGTH_SHORT).show()
            }
            true
        }
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

    fun initializingGridViewFiles()
    {
        lm = GridLayoutManager(this ,3)

        lm.reverseLayout = false

        chatImagesAdapter = ChatImagesAdapter()

        binding.rvChat.adapter = chatImagesAdapter

        binding.rvChat.layoutManager = lm

        if (currentChannelId != null)
        {
            getImagesFromFireBase(currentChannelId)
        }

    }

    fun sendMessage(channelId:String, message:Message)
    {
        chatChannelsCollectionRef.document(channelId).collection("messages").add(message)
        if(message is TextMessage)
        {
            chatChannelsCollectionRef.document(channelId).collection("messages").document("lastMessage")
                .get().addOnSuccessListener {
                        document->
                    if (document.exists())
                    {
                        chatChannelsCollectionRef.document(channelId).collection("messages").document("lastMessage").update(
                            mapOf("date" to message.date
                                ,"message" to message.message
                                ,"type" to message.type))
                    }
                    else
                    {
                        chatChannelsCollectionRef.document(channelId).collection("messages").document("lastMessage").set(
                            mapOf("date" to message.date
                                ,"message" to message.message
                                ,"type" to message.type))
                    }
                }
        }
        else if (message is ImageMessage)
        {

            chatChannelsCollectionRef.document(channelId).collection("messages").document("lastMessage")
                .get().addOnSuccessListener {
                        document->
                    if (document.exists())
                    {
                        chatChannelsCollectionRef.document(channelId).collection("messages").document("lastMessage").update(
                            mapOf("date" to message.date
                                ,"message" to "send Picture"
                                ,"type" to message.type))
                    }
                    else
                    {
                        chatChannelsCollectionRef.document(channelId).collection("messages").document("lastMessage").set(
                            mapOf("date" to message.date
                                ,"message" to "send Picture"
                                ,"type" to message.type))
                    }
                }
        }
        else if (message is VoiceMessage)
        {
            chatChannelsCollectionRef.document(channelId).collection("messages").document("lastMessage")
                .get().addOnSuccessListener {
                        document->
                    if (document.exists())
                    {
                        chatChannelsCollectionRef.document(channelId).collection("messages").document("lastMessage").update(
                            mapOf("date" to message.date
                                ,"message" to "send voice message"
                                ,"type" to message.type))
                    }
                    else
                    {
                        chatChannelsCollectionRef.document(channelId).collection("messages").document("lastMessage").set(
                            mapOf("date" to message.date
                                ,"message" to "send voice message"
                                ,"type" to message.type))
                    }
                }
        }


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
            initializingRecyclerView()
            GlobalScope.launch {
                async {
                    getMessageFromFireBase(currentChannelId)
                }
            }

        }

        binding.toolBarBuFiles.setOnClickListener {
            binding.toolBarBuFiles.setTextColor(Color.parseColor("#51BA65"))
            binding.toolBarBuFiles.setBackgroundResource(R.drawable.shape_circle)
            binding.toolBarBuChat.setTextColor(Color.parseColor("#FFFFFFFF"))
            binding.toolBarBuChat.setBackgroundResource(R.color.my_green)
            initializingGridViewFiles()
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
    fun getImagesFromFireBase(channelId: String)
    {

        val arrayOfImages = arrayListOf<String>()
        val query = chatChannelsCollectionRef.document(channelId).collection("messages").orderBy("date" ,Query.Direction.DESCENDING)
        query.addSnapshotListener { querySnapshot, error ->
            messageAdapter.arrayOfMessages.clear()
            querySnapshot!!.documents.forEach {
                    document ->
                if (document.id == "lastMessage")
                {
                    return@forEach
                }
                if (document["type"] == MessageType.TEXT)
                {
                   return@forEach
                }
                else
                {
                    arrayOfImages.add(document.toObject(ImageMessage::class.java)!!.imagePath)
                    Log.d("chat" ,ReceiveMessage(document.toObject(TextMessage::class.java)!!,document.id).toString())
                }
            }
            chatImagesAdapter.setList(arrayOfImages)

            binding.rvChat.scrollToPosition(0)
        }
    }


    fun getMessageFromFireBase(channelId: String)
    {
        runOnUiThread {
            binding.animationLoadingMessages.visibility = View.VISIBLE
        }
        val arrayOfReceiveMessage = arrayListOf<ReceiveMessage>()

                val query = chatChannelsCollectionRef.document(channelId).collection("messages").orderBy("date" ,Query.Direction.DESCENDING)
                query.addSnapshotListener { querySnapshot, error ->
                    runOnUiThread {
                        binding.animationLoadingMessages.visibility = View.GONE
                    }
                    messageAdapter.arrayOfMessages.clear()
                    querySnapshot!!.documents.forEach {
                            document ->
                        if (document.id == "lastMessage")
                        {
                            return@forEach
                        }
                        if (document["type"] == MessageType.TEXT)
                        {
                            arrayOfReceiveMessage.add(ReceiveMessage(document.toObject(TextMessage::class.java)!!,document.id))
                            Log.d("chat" ,ReceiveMessage(document.toObject(TextMessage::class.java)!!,document.id).toString())
                        }
                        else if (document["type"] == MessageType.IMAGE)
                        {
                            arrayOfReceiveMessage.add(ReceiveMessage(document.toObject(ImageMessage::class.java)!!,document.id))
                            Log.d("chat" ,ReceiveMessage(document.toObject(TextMessage::class.java)!!,document.id).toString())
                        }
                        else
                        {
                            arrayOfReceiveMessage.add(ReceiveMessage(document.toObject(VoiceMessage::class.java)!!,document.id))
                            Log.d("chat" ,ReceiveMessage(document.toObject(VoiceMessage::class.java)!!,document.id).toString())
                        }
                    }
                    messageAdapter.setList(arrayOfReceiveMessage )

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



    private fun startRecording() {
        recorder = MediaRecorder()
        recorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
        recorder!!.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
        recorder!!.setOutputFile(fileName)
        recorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        try {
            recorder!!.prepare()
        } catch (e: IOException) {
            Log.e("LOG_TAG", "prepare() failed" + e.message)
        }
        recorder!!.start()
        binding.voiceMessageContainer.visibility = View.VISIBLE
        binding.voiceMessageContainer.alpha = 0.0f

        binding.voiceMessageContainer.animate()
            .translationY(0.0f)
            .alpha(1.0f)
            .setListener(null)


        val audioRecordView = binding.audioRecordView

        binding.chronometerMessageDelay.start()

        timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                    val currentMaxAmplitude = recorder!!.maxAmplitude
                    audioRecordView.update(currentMaxAmplitude) //redraw view
            }
        }, 0, 100)

    }

    private fun stopRecording() {

        try {
            timer.cancel()
            recorder!!.stop()
            recorder!!.release()
            recorder = null
            binding.voiceMessageContainer.animate()
                .translationY(binding.audioRecordView.height.toFloat())
                .alpha(0.0f)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        binding.voiceMessageContainer.visibility = View.GONE
                    }
                })
            binding.audioRecordView.recreate()

            binding.chronometerMessageDelay.base = SystemClock.elapsedRealtime()
            binding.chronometerMessageDelay.stop()
        } catch (stopException: RuntimeException) {
            Log.d("LOG_TAG", " message derreure " + stopException.message)
        }
        uploadAudio()
    }

    fun fileRecordingInit()
    {
        fileName = "${externalCacheDir!!.absolutePath}/audiorecord.3gp"
    }

    private fun uploadAudio() {
        val voiceUID = UUID.randomUUID().toString()
        val fii: StorageReference = storageInstance.reference.child("Audio").child(voiceUID)
        val uri = Uri.fromFile(File(fileName))
            fii.putFile(uri).addOnCompleteListener{
                if (it.isSuccessful)
                {
                    fii.downloadUrl.addOnCompleteListener {
                            task->
                        if (task.isSuccessful)
                        {
                            sendMessage(currentChannelId ,VoiceMessage(task.result.toString() ,currentUserUID ,currentFriend.uid ,appPref.getCurrentUserName() ,currentFriend.name ,Calendar.getInstance().time))
                        }
                        else
                        {
                            Toast.makeText(this ,"something went wrong please try again later" ,Toast.LENGTH_LONG).show()
                        }
                    }
                }
                else
                {
                    Toast.makeText(this ,"something went wrong please try again later" ,Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun checkPermissionRecordAudio() {
        if (ContextCompat.checkSelfPermission(this, arrayOf(Manifest.permission.RECORD_AUDIO).toString()) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), PERMISSIONS_CODES.RECORD_AUDIO)
        }

    }

    private fun checkPermissionCamera() {
        if (ContextCompat.checkSelfPermission(this, arrayOf(Manifest.permission.CAMERA).toString()) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), PERMISSIONS_CODES.CAMERA)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSIONS_CODES.RECORD_AUDIO)
        {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                checkPermissionCamera()
            }
            else if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_DENIED)
            {
                intentToSettings()
            }

        }

        if (requestCode == PERMISSIONS_CODES.CAMERA)
        {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                return
            }
            else if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_DENIED)
            {
                intentToSettings()
            }

        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
    fun intentToSettings()
    {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    override fun onStart() {
        checkPermissionRecordAudio()
        super.onStart()
    }
}