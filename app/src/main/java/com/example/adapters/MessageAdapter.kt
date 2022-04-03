package com.example.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.messenger.R
import com.example.pojo.ReceiveMessage
class MessageAdapter: RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    var arrayOfMessages = arrayListOf<ReceiveMessage>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        return MessageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.message_item ,parent ,false))
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