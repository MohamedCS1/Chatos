package com.example.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.messenger.R
import com.example.pojo.User
import de.hdodenhof.circleimageview.CircleImageView

class RandomPeopleAdapter:RecyclerView.Adapter<RandomPeopleAdapter.RandomPeopleViewHolder>() {

    var arrayOfUsers = arrayListOf<User>()
    lateinit var context:Context
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RandomPeopleViewHolder {
        context = parent.context
        return RandomPeopleViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.random_people_item ,parent ,false))
    }

    override fun onBindViewHolder(holder: RandomPeopleViewHolder, position: Int) {
        Glide.with(context).load(arrayOfUsers[position].imagePath).into(holder.imageProfile)
        holder.name.text = arrayOfUsers[position].name
        holder.job.text = arrayOfUsers[position].job
        holder.country.text = arrayOfUsers[position].country
    }

    override fun getItemCount(): Int {
        return arrayOfUsers.size
    }

    class RandomPeopleViewHolder(itemView:View):RecyclerView.ViewHolder(itemView)
    {
        val imageProfile = itemView.findViewById<CircleImageView>(R.id.rp_photoProfile)
        val name = itemView.findViewById<TextView>(R.id.rp_name)
        val job = itemView.findViewById<TextView>(R.id.rp_job)
        val country = itemView.findViewById<TextView>(R.id.rp_country)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setList(arrayofusers:ArrayList<User>)
    {
        this.arrayOfUsers = arrayofusers
        notifyDataSetChanged()
    }
}