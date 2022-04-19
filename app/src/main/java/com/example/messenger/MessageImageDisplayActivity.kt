package com.example.messenger

import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.davemorrissey.labs.subscaleview.ImageSource
import com.example.messenger.databinding.ActivityMessageImageDisplayBinding

class MessageImageDisplayActivity : AppCompatActivity() {

    lateinit var binding:ActivityMessageImageDisplayBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessageImageDisplayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imagePath = intent.extras?.getString("imagePath")

        Glide.with(this).load(imagePath).centerInside().placeholder(R.drawable.ic_photo_placeholder).into(object:SimpleTarget<Drawable>(){
            override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                binding.imageView.setImage(ImageSource.bitmap(resource.toBitmap()))
            }
        } )
    }

}