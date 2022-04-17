package com.example.messenger

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.adapters.SearchAdapter
import com.example.messenger.databinding.ActivitySearchBinding
import com.example.pojo.User
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class SearchActivity : AppCompatActivity() {

    val fireStore:FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }
    lateinit var searchAdapter:SearchAdapter

    lateinit var binding: ActivitySearchBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        searchAdapter = SearchAdapter()

        initializeRecyclerView()

        binding.buBack.setOnClickListener {
            onBackPressed()
        }

        binding.buClearText.setOnClickListener {
            binding.etSearch.setText("")
        }

        binding.etSearch.isFocusableInTouchMode = true
        binding.etSearch.isFocusable = true
        binding.etSearch.requestFocus()

        binding.etSearch.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchAdapter.clearArray()
                if (s!!.isEmpty())
                {
                    Toast.makeText(this@SearchActivity ,"Empty" ,Toast.LENGTH_SHORT).show()
                    searchAdapter.clearArray()
                }
                else
                {
                    fireStore.collection("users")
                        .orderBy("name").get().addOnSuccessListener {
                            it.documents.forEach {
                                if (it.toObject(User::class.java)!!.name.lowercase().contains(s.toString().lowercase()))
                                {
                                    searchAdapter.addUser(it.toObject(User::class.java)!!)
                                }
                            }
                        }

                }
            }

            override fun afterTextChanged(s: Editable?) {
                if (!s.isNullOrBlank())
                {
                    binding.buClearText.visibility = View.VISIBLE
                    return
                }
                binding.buClearText.visibility = View.INVISIBLE
            }
        })
        searchAdapter.setOnPersonClick(object :SearchAdapter.SetOnUserClick{
            override fun userValue(user: User) {
                Toast.makeText(this@SearchActivity ,"Clicked" ,Toast.LENGTH_SHORT).show()
                val intentToPublicProfile = Intent(this@SearchActivity ,PublicProfileActivity::class.java)
                intentToPublicProfile.putExtra("currentFriend" ,user)
                startActivity(intentToPublicProfile)
            }
        })
    }


    fun initializeRecyclerView()
    {
        binding.rvSearch.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = searchAdapter
        }
    }

}