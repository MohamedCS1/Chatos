package com.example.messenger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.messenger.databinding.ActivityPublicProfileBinding

class PublicProfileActivity : AppCompatActivity() {

    lateinit var binding: ActivityPublicProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPublicProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buBack.setOnClickListener {
            onBackPressed()
        }

    }
}