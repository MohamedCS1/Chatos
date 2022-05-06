package com.example.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.messenger.R
import com.example.pojo.Message
import com.example.pojo.ReceiveMessage
import com.example.pojo.TextMessage
import com.example.pojo.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.getField
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class PersonAdapter: RecyclerView.Adapter<PersonAdapter.PersonViewHolder>() {

    var arrayOfPersons = arrayListOf<User>()
    var setOnPersonClick:SetOnPersonClick? = null
    val mAuth:FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    private val fireStoreInstance: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    private val chatChannelsCollectionRef = fireStoreInstance.collection("chatChannels")

    lateinit var context: Context
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonViewHolder {
        context = parent.context
        return PersonViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.rv_person_item ,null ,false))
    }

    override fun onBindViewHolder(holder: PersonViewHolder, position: Int) {
        holder.name.text = arrayOfPersons[position].name
//        holder.lastMessage.text = arrayOfPersons[position].lastMessage
        Glide.with(context).load(arrayOfPersons[position].imagePath)
            .placeholder(R.drawable.ic_photo_placeholder).into(holder.image)
        holder.itemView.setOnClickListener {
            setOnPersonClick!!.personValue(arrayOfPersons[position])
        }

        fireStoreInstance.collection("users")
            .document(arrayOfPersons[position].uid)
            .collection("sharedChat")
            .document(mAuth.currentUser!!.uid)
            .get().addOnSuccessListener { document ->

                chatChannelsCollectionRef.document(document["channelId"].toString())
                    .collection("messages").document("/lastMessage").get().addOnSuccessListener {
                        holder.lastMessage.text = it["message"].toString() +" "+it["date"].toString()
                    }
            }
    }

    override fun getItemCount(): Int {
        return arrayOfPersons.size
    }

    class PersonViewHolder(itemView:View):RecyclerView.ViewHolder(itemView)
    {
        val image = itemView.findViewById<ImageView>(R.id.person_image)
        val name = itemView.findViewById<TextView>(R.id.person_name)
        val lastMessage = itemView.findViewById<TextView>(R.id.person_last_message)
    }


    fun setOnPersonClick(setOnPersonClick: SetOnPersonClick)
    {
        this.setOnPersonClick = setOnPersonClick
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setList(arrayPersons:ArrayList<User>)
    {
        this.arrayOfPersons = arrayPersons
        notifyDataSetChanged()
    }

    interface SetOnPersonClick{
        fun personValue(User: User)
    }
}