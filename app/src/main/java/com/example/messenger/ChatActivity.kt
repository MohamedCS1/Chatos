package com.example.messenger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.pojo.Person

class ChatActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val bundle = intent.extras

        val person = bundle?.get("person") as Person

        Toast.makeText(this ,person.toString() ,Toast.LENGTH_SHORT).show()

    }
}