package com.example.messenger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.messenger.databinding.ActivityPublicProfileBinding
import com.example.pojo.User
import com.example.sharedPreferences.AppSharedPreferences
import com.google.firebase.firestore.FirebaseFirestore

class PublicProfileActivity : AppCompatActivity() {


    private val fireStoreInstance: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }
    private lateinit var appPref: AppSharedPreferences

    lateinit var binding: ActivityPublicProfileBinding

    lateinit var currentUserUID:String

    private lateinit var people:User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPublicProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bundle = intent.extras

        appPref = AppSharedPreferences()

        appPref.PrefManager(this)

        currentUserUID = appPref.getCurrentUserUID()

        people = bundle?.get("currentFriend") as User

        Toast.makeText(this ,people.uid+" "+currentUserUID ,Toast.LENGTH_SHORT).show()

        binding.buBack.setOnClickListener {
            onBackPressed()
        }

        Glide.with(this).load(people.imagePath).placeholder(R.drawable.ic_photo_placeholder)
            .into(binding.imageviewPhotoProfile)

        binding.name.text = people.name
        binding.tvUserCountry.text = people.country
        binding.tvUserJob.text = people.job

        binding.buttonNewMessage.setOnClickListener {
            createChatChannel()
        }

    }

    fun createChatChannel() {
        val chatChannel = fireStoreInstance.collection("users").document()

        fireStoreInstance.collection("users")
            .document(currentUserUID)
            .collection("sharedChat")
            .document(people.uid)
            .get().addOnSuccessListener { document ->
                if (document.exists()) {
                    return@addOnSuccessListener
                }
                fireStoreInstance.collection("users")
                    .document(people.uid)
                    .collection("sharedChat")
                    .document(currentUserUID)
                    .set(mapOf("channelId" to chatChannel.id))

                fireStoreInstance.collection("users")
                    .document(currentUserUID)
                    .collection("sharedChat")
                    .document(people.uid)
                    .set(mapOf("channelId" to chatChannel.id))
            }
    }
}