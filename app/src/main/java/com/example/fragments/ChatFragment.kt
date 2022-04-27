package com.example.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.adapters.PersonAdapter
import com.example.messenger.ChatActivity
import com.example.messenger.R
import com.example.pojo.User
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

    lateinit var animationView:LottieAnimationView

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
        appPref.PrefManager(activity?.baseContext)

        animationView = view.findViewById(R.id.progress_animation_view)

        addChatListener()
        val rv = view.findViewById<RecyclerView>(R.id.rv_person)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = personAdapter

        personAdapter.setOnPersonClick(object :PersonAdapter.SetOnPersonClick{
            override fun personValue(user: User) {
                val intentToChatActivity = Intent(requireContext() ,ChatActivity::class.java)
                intentToChatActivity.putExtra("person" ,user)
                startActivity(intentToChatActivity)
            }
        })
        return view
    }


    fun addChatListener()
    {
        Log.d("currentChatFragment" ,"Listining")

//        val arrayOfPersons = arrayListOf<Person>()
        val arrayOfFriends = arrayListOf<User>()
        fireStore.collection("users").document(appPref.getCurrentUserUID()).collection("sharedChat").addSnapshotListener(object :EventListener<QuerySnapshot>{
            override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {

                personAdapter.arrayOfPersons.clear()
                if (error != null) {
                    Toast.makeText(requireContext(), error.message.toString(), Toast.LENGTH_SHORT).show()
                    return
                }
                value?.documents?.forEach {
                    fireStore.document("users/${it.id}").get().addOnSuccessListener {
                        Log.d("currentChatFragment" ,it.toString())
                        arrayOfFriends.add(User(it.id ,it["name"].toString() ,it["email"].toString() ,it["password"].toString() ,it["imagePath"].toString() ,it["job"].toString() ,it["country"].toString() ,it["gender"].toString()))
                    }.addOnCompleteListener {
                        if (it.isSuccessful)
                        {
                            personAdapter.setList(arrayOfFriends)
                            animationView.visibility = View.GONE
                        }

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