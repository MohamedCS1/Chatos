package com.example.messenger

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.messenger.databinding.ActivitySearchBinding

class SearchActivity : AppCompatActivity() {
    lateinit var binding: ActivitySearchBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
//
//    fun initializeRecyclerView()
//    {
//
//    }

}