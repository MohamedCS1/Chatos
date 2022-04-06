package com.example.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.messenger.R
import com.example.pojo.ReceiveMessage

private const val VIEW_TYPE_MY_MESSAGE = 1
private const val VIEW_TYPE_OTHER_MESSAGE = 2
class MessageAdapter(val currentId:String): RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    var arrayOfMessages = arrayListOf<ReceiveMessage>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        if (viewType == VIEW_TYPE_MY_MESSAGE)
        {
            return MessageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.message_item_sender ,parent ,false))
        }
        else
        {
            return MessageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.message_item_receiver ,parent ,false))
        }
    }

    override fun getItemViewType(position: Int): Int {
        val messageSenderId = arrayOfMessages[position].message.senderId

        if(currentId == messageSenderId) {
            return VIEW_TYPE_MY_MESSAGE
        }
        else {
            return VIEW_TYPE_OTHER_MESSAGE
        }
    }
    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.message.text = arrayOfMessages[position].message.message
        holder.date.text = android.text.format.DateFormat.format("hh:mm a" ,arrayOfMessages[position].message.date).toString()
    }

    override fun getItemCount(): Int {
        return arrayOfMessages.size
    }

    class MessageViewHolder(itemView:View):RecyclerView.ViewHolder(itemView)
    {
        val message = itemView.findViewById<TextView>(R.id.tv_message)
        val date = itemView.findViewById<TextView>(R.id.tv_date_message)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setList(arrayofmessages:ArrayList<ReceiveMessage>)
    {
        this.arrayOfMessages = arrayofmessages
        notifyDataSetChanged()
    }


}