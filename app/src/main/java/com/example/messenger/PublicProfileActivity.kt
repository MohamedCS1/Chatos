package com.example.messenger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.example.messenger.databinding.ActivityPublicProfileBinding

class PublicProfileActivity : AppCompatActivity() {

    lateinit var binding: ActivityPublicProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPublicProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val bundle = intent.extras

        val imageProfilePath = bundle?.getString("photoProfilePath","")
        val name = bundle?.getString("currentFriendName","")
        binding.buBack.setOnClickListener {
            onBackPressed()
        }

        Glide.with(this).load(imageProfilePath).placeholder(R.drawable.ic_photo_placeholder).into(binding.imageviewPhotoProfile)

        binding.name.text = name
    }
}