package com.example.messenger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.example.messenger.databinding.ActivityPublicProfileBinding
import com.example.pojo.User

class PublicProfileActivity : AppCompatActivity() {

    lateinit var binding: ActivityPublicProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPublicProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val bundle = intent.extras

        val user = bundle?.get("currentFriend") as User
        binding.buBack.setOnClickListener {
            onBackPressed()
        }

        Glide.with(this).load(user.imagePath).placeholder(R.drawable.ic_photo_placeholder).into(binding.imageviewPhotoProfile)

        binding.name.text = user.name
        binding.tvUserCountry.text = user.country
        binding.tvUserJob.text = user.job
    }
}