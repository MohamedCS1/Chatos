package com.example.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterInside
import com.example.messenger.MessageImageDisplayActivity
import com.example.messenger.R
import com.example.pojo.ImageMessage
import com.example.pojo.MessageType
import com.example.pojo.ReceiveMessage
import com.example.pojo.TextMessage

private const val VIEW_TYPE_MY_MESSAGE = 1
private const val VIEW_TYPE_OTHER_MESSAGE = 2

private const val VIEW_TYPE_MY_MESSAGE_IMAGE = 3
private const val VIEW_TYPE_OTHER_MESSAGE_IMAGE = 4

class MessageAdapter(val currentId:String): RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    var arrayOfMessages = arrayListOf<ReceiveMessage>()
    lateinit var context:Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        context = parent.context
        if (viewType == VIEW_TYPE_MY_MESSAGE)
        {
            return MessageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.message_item_sender ,parent ,false))
        }
        else if (viewType == VIEW_TYPE_OTHER_MESSAGE)
        {
            return MessageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.message_item_receiver ,parent ,false))
        }
        else if (viewType == VIEW_TYPE_MY_MESSAGE_IMAGE)
        {
            return MessageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.message_image_item_sender ,parent ,false))
        }
        else
        {
            return MessageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.message_image_item_receiver ,parent ,false))
        }

    }

    override fun getItemViewType(position: Int): Int {
        val message = arrayOfMessages[position]

        if (message.Message.type == MessageType.TEXT)
        {
            if(currentId == message.Message.senderId) {
                return VIEW_TYPE_MY_MESSAGE
            }
            else {
                return VIEW_TYPE_OTHER_MESSAGE
            }
        }
        else
        {
            if(currentId == message.Message.senderId) {
                return VIEW_TYPE_MY_MESSAGE_IMAGE
            }
            else {
                return VIEW_TYPE_OTHER_MESSAGE_IMAGE
            }
        }

    }
    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        if (arrayOfMessages[position].Message.type == MessageType.TEXT)
        {
            val textMessage = arrayOfMessages[position].Message as TextMessage
            holder.message.text = textMessage.message
            holder.date.text = android.text.format.DateFormat.format("hh:mm a" ,textMessage.date.time).toString()
        }
        else if (arrayOfMessages[position].Message.type == MessageType.IMAGE)
        {
            val imageMessage = arrayOfMessages[position].Message as ImageMessage
            Glide.with(context).load(imageMessage.imagePath).override(800, 1080)
                .placeholder(R.drawable.ic_photo_placeholder).transform(CenterInside(),com.bumptech.glide.load.resource.bitmap.RoundedCorners(27)).into(holder.image)

        }
        holder.itemView.setOnClickListener {
            if (holder.date.visibility == View.VISIBLE)
            {
                holder.date.visibility = View.GONE
            }
            else
            {
                holder.date.visibility = View.VISIBLE
            }
            if (arrayOfMessages[position].Message.type == MessageType.IMAGE)
            {
                val imageMessage = arrayOfMessages[position].Message as ImageMessage
                val intentToImageDisplay = Intent(context ,MessageImageDisplayActivity::class.java)
                intentToImageDisplay.putExtra("imagePath" ,imageMessage.imagePath)
                context.startActivity(intentToImageDisplay)
            }
        }

        if (position < 1)
        {
            val animation: Animation = AnimationUtils.loadAnimation(context, R.anim.rv_chat_animation)
            holder.itemView.startAnimation(animation)
        }

    }

    override fun getItemCount(): Int {
        return arrayOfMessages.size
    }

    class MessageViewHolder(itemView:View):RecyclerView.ViewHolder(itemView)
    {
        val message = itemView.findViewById<TextView>(R.id.tv_message)
        val date = itemView.findViewById<TextView>(R.id.tv_date_message)
        val image = itemView.findViewById<ImageView>(R.id.imageMessage)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setList(arrayofmessages:ArrayList<ReceiveMessage>)
    {
        this.arrayOfMessages = arrayofmessages
        notifyDataSetChanged()

    }


}