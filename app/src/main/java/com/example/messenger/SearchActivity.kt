package com.example.messenger

import android.annotation.SuppressLint
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
    }


    fun initializeRecyclerView()
    {
        binding.rvSearch.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = searchAdapter
        }
    }

}