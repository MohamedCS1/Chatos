package com.example.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.messenger.MessageImageDisplayActivity
import com.example.messenger.R
import com.example.pojo.ImageMessage
import com.example.pojo.User
import java.util.ArrayList

class ChatImagesAdapter:RecyclerView.Adapter<ChatImagesAdapter.ChatImagesViewHolder>() {

    lateinit var context: Context
    var arrayOfImages = arrayListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatImagesViewHolder {
        context = parent.context
        return ChatImagesViewHolder(View.inflate(context ,R.layout.rv_images_item,null))
    }

    override fun onBindViewHolder(holder: ChatImagesViewHolder, position: Int) {
        Glide.with(context).load(arrayOfImages[position]).centerInside().into(holder.image)

        holder.itemView.setOnClickListener {
            val image = arrayOfImages[position]
            val intentToImageDisplay = Intent(context , MessageImageDisplayActivity::class.java)
            intentToImageDisplay.putExtra("imagePath" ,image)
            context.startActivity(intentToImageDisplay)
        }
    }

    override fun getItemCount(): Int {
        return arrayOfImages.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setList(arrayImages: ArrayList<String>)
    {
        this.arrayOfImages = arrayImages
        notifyDataSetChanged()
    }

    class ChatImagesViewHolder(itemView:View):RecyclerView.ViewHolder(itemView)
    {
        val image = itemView.findViewById<ImageView>(R.id.chatImages)
    }
}