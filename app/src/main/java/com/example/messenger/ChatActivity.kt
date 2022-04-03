package com.example.messenger

import android.content.Context
import android.graphics.Color
import android.graphics.LinearGradient
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.adapters.MessageAdapter
import com.example.messenger.databinding.ActivityChatBinding
import com.example.pojo.Message
import com.example.pojo.Person
import com.example.pojo.ReceiveMessage
import com.example.sharedPreferences.AppSharedPreferences
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import java.util.*


class ChatActivity : AppCompatActivity() {

    lateinit var binding: ActivityChatBinding
    lateinit var context: Context
    lateinit var appPref: AppSharedPreferences
    lateinit var person:Person
    lateinit var messageAdapter:MessageAdapter

    private val fireStoreInstance:FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    private val chatChannelsCollectionRef = fireStoreInstance.collection("chatChannels")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        context = this

        appPref = AppSharedPreferences()

        appPref.PrefManager(this)

        messageAdapter = MessageAdapter()

        val bundle = intent.extras

        person = bundle?.get("person") as Person

        createChatChannel()
        {
            channelId ->
            getMessageFromFireBase(channelId)
            binding.buSendMessage.setOnClickListener {
            if (binding.edittextSendMessage.text.isNotBlank() && binding.edittextSendMessage.text.isNotEmpty())
            {
                sendMessage(channelId,Message(binding.edittextSendMessage.text.toString() ,appPref.getUID() ,Calendar.getInstance().time))
                binding.edittextSendMessage.setText(" ")
            }
        }

        }

        bottomToolbarSendMessageAnimation()

        buChatTollBarSelected()

        Toast.makeText(this ,person.uid ,Toast.LENGTH_SHORT).show()

        binding.tvUsername.text = person.name
        Glide.with(this).load(person.imagePath).placeholder(R.drawable.ic_photo_placeholder).into(binding.imageviewPhotoProfile)

        binding.rvChat.adapter = messageAdapter
        binding.rvChat.layoutManager = LinearLayoutManager(this)
    }

    fun sendMessage(channelId:String ,message:Message)
    {
        chatChannelsCollectionRef.document(channelId).collection("messages").add(message)
    }

    fun createChatChannel(onComplete:(channelId:String) -> Unit)
    {
        val currentUserId = appPref.getUID()
        val chatChannel = fireStoreInstance.collection("users").document()

        fireStoreInstance.collection("users")
            .document(currentUserId)
            .collection("sharedChat")
            .document(person.uid)
            .get().addOnSuccessListener {
                document ->
                if (document.exists()) {
                    onComplete(document["channelId"].toString())
                    return@addOnSuccessListener
                }
        fireStoreInstance.collection("users")
            .document(person.uid)
            .collection("sharedChat")
            .document(currentUserId)
            .set(mapOf("channelId" to chatChannel.id))

        fireStoreInstance.collection("users")
            .document(currentUserId)
            .collection("sharedChat")
            .document(person.uid)
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
        val query = chatChannelsCollectionRef.document(channelId).collection("message")
        query.addSnapshotListener { querySnapshot, error ->
            querySnapshot!!.documents.forEach {
                document ->
                arrayOfReceiveMessage.add(ReceiveMessage(document.toObject(Message::class.java)!!,document.id))
            }
            messageAdapter.setList(arrayOfReceiveMessage)
        }
    }

}