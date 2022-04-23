package com.example.fragments

import android.os.Bundle
import android.os.RecoverySystem
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.adapters.RandomPeopleAdapter
import com.example.messenger.R
import com.example.pojo.User
import com.example.sharedPreferences.AppSharedPreferences
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot


class ExploreFragment : Fragment() {

    private val fireStore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    lateinit var appPref: AppSharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_explore, container, false)

        appPref = AppSharedPreferences()
        appPref.PrefManager(activity?.baseContext)

        val rv = view.findViewById<RecyclerView>(R.id.rv_random_people)
        val randomPeopleAdapter = RandomPeopleAdapter()
        val lm = GridLayoutManager(context,2)

        rv.adapter = randomPeopleAdapter
        rv.layoutManager = lm

        val arrayOfFriends = arrayListOf<User>()

        fireStore.collection("users").get().addOnSuccessListener {
            it.forEach {
                fireStore.document("users/${it.id}").get().addOnSuccessListener {
                    Log.d("currentChatFragment" ,it.toString())
                    arrayOfFriends.add(User(it.id ,it["name"].toString() ,it["email"].toString() ,it["password"].toString() ,it["imagePath"].toString() ,it["job"].toString() ,it["country"].toString() ,it["gender"].toString()))
                }.addOnCompleteListener {
                    randomPeopleAdapter.setList(arrayOfFriends)
                }
            }
        }

        return view
    }
}