package com.example.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.adapters.PersonAdapter
import com.example.messenger.ChatActivity
import com.example.messenger.R
import com.example.messenger.SearchActivity
import com.example.pojo.Person
import com.example.sharedPreferences.AppSharedPreferences
import com.google.firebase.firestore.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ChatFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private val fireStore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    private val personAdapter:PersonAdapter by lazy {
        PersonAdapter()
    }

    lateinit var appPref:AppSharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =  inflater.inflate(R.layout.fragment_chat, container, false)
        appPref = AppSharedPreferences()
        appPref.PrefManager(requireContext())
        addChatListener()
        val rv = view.findViewById<RecyclerView>(R.id.rv_person)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = personAdapter

        personAdapter.setOnPersonClick(object :PersonAdapter.SetOnPersonClick{
            override fun personValue(person: Person) {
                val intentToChatActivity = Intent(requireContext() ,ChatActivity::class.java)
                intentToChatActivity.putExtra("person" ,person)
                startActivity(intentToChatActivity)
            }
        })
        return view
    }


    fun addChatListener()
    {

//        val arrayOfPersons = arrayListOf<Person>()
        val arrayOfFriends = arrayListOf<Person>()
        fireStore.collection("users").document(appPref.getCurrentUserUID()).collection("sharedChat").addSnapshotListener(object :EventListener<QuerySnapshot>{
            override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {

                personAdapter.arrayOfPersons.clear()
                if (error != null) {
                    Toast.makeText(requireContext(), error.message.toString(), Toast.LENGTH_SHORT).show()
                    return
                }
                value?.documents?.forEach {
                    fireStore.document("users/${it.id}").get().addOnSuccessListener {
                        arrayOfFriends.add(Person(it.id ,it["name"].toString() ,it["imagePath"].toString()  ,it.get("lastMessage").toString()))
                    }.addOnCompleteListener {
                        personAdapter.setList(arrayOfFriends)
                    }
                }
            }


        })
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ChatFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}