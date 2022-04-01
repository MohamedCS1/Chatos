package com.example.messenger

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.marginRight
import com.bumptech.glide.Glide
import com.example.messenger.databinding.ActivityChatBinding
import com.example.pojo.Person
import com.example.sharedPreferences.AppSharedPreferences
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage


class ChatActivity : AppCompatActivity() {

    lateinit var binding: ActivityChatBinding
    lateinit var context: Context
    lateinit var appPref: AppSharedPreferences
    lateinit var person:Person

    private val fireStoreInstance:FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        context = this

        appPref = AppSharedPreferences()

        appPref.PrefManager(this)

        val bundle = intent.extras

        person = bundle?.get("person") as Person

        createChatChannel()

        bottomToolbarSendMessageAnimation()

        buChatTollBarSelected()

        Toast.makeText(this ,person.uid ,Toast.LENGTH_SHORT).show()

        binding.buSendMessage.setOnClickListener {
            if (binding.edittextSendMessage.text.isNotBlank() && binding.edittextSendMessage.text.isNotEmpty())
            {
                sendMessage(binding.edittextSendMessage.text.toString())
            }
        }


        binding.tvUsername.text = person.name
        Glide.with(this).load(person.imagePath).placeholder(R.drawable.ic_photo_placeholder).into(binding.imageviewPhotoProfile)

    }

    fun sendMessage(Message:String)
    {

    }

    fun createChatChannel()
    {
        val currentUserId = appPref.getUID()
        val newChatChannel = fireStoreInstance.collection("users").document()

        fireStoreInstance.collection("users")
            .document(person.uid)
            .collection("sharedChat")
            .document(currentUserId)
            .set(mapOf("channelID" to newChatChannel.id))

        fireStoreInstance.collection("users")
            .document(currentUserId)
            .collection("sharedChat")
            .document(person.uid)
            .set(mapOf("channelID" to newChatChannel.id))

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

}