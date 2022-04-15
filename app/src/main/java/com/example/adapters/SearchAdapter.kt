package com.example.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.messenger.R
import com.example.pojo.Person

class SearchAdapter :RecyclerView.Adapter<SearchAdapter.PersonViewHolder>(){
    var arrayOfPersons = arrayListOf<Person>()
    var setOnPersonClick:SetOnPersonClick? = null


    lateinit var context: Context
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonViewHolder {
        context = parent.context
        return PersonViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.rv_person_item ,null ,false))
    }

    override fun onBindViewHolder(holder: PersonViewHolder, position: Int) {
        holder.name.text = arrayOfPersons[position].name
//        holder.lastMessage.text = arrayOfPersons[position].lastMessage
        Glide.with(context).load(arrayOfPersons[position].photoProfilePath).placeholder(R.drawable.ic_photo_placeholder).into(holder.image)
        holder.itemView.setOnClickListener {
            setOnPersonClick!!.personValue(arrayOfPersons[position])
        }
    }

    override fun getItemCount(): Int {
        return arrayOfPersons.size
    }

    class PersonViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)
    {
        val image = itemView.findViewById<ImageView>(R.id.person_image)
        val name = itemView.findViewById<TextView>(R.id.person_name)
        val job = itemView.findViewById<TextView>(R.id.person_job)
    }


    fun setOnPersonClick(setOnPersonClick: SetOnPersonClick)
    {
        this.setOnPersonClick = setOnPersonClick
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setList(arrayPersons:ArrayList<Person>)
    {
        this.arrayOfPersons = arrayPersons
        notifyDataSetChanged()
    }

    interface SetOnPersonClick{
        fun personValue(person: Person)
    }

}