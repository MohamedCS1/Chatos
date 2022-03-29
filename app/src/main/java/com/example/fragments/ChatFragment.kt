package com.example.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.adapters.PersonAdapter
import com.example.messenger.ChatActivity
import com.example.messenger.R
import com.example.pojo.Person
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
        val arrayOfPersons = arrayListOf<Person>()
        fireStore.collection("users/").addSnapshotListener(object :EventListener<QuerySnapshot>{
            override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                if (error != null) {
                    Toast.makeText(requireContext(), error.message.toString(), Toast.LENGTH_SHORT)
                        .show()
                    return
                }
                value?.documents?.forEach {
                    arrayOfPersons.add(Person(it["imagePath"].toString() ,it["name"].toString() ,
                        it.get("lastMessage").toString()
                    ))
                }
                personAdapter.setList(arrayOfPersons)
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