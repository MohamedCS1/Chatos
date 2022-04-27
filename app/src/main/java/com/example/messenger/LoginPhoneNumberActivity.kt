package com.example.messenger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.example.messenger.databinding.ActivityLoginPhoneNumberBinding

class LoginPhoneNumberActivity : AppCompatActivity() {

    lateinit var binding:ActivityLoginPhoneNumberBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginPhoneNumberBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buNext.setOnClickListener {
            val alerDialog = AlertDialog.Builder(this)

            val otpView = LayoutInflater.from(this).inflate(R.layout.dialog_otp ,null ,false)

            alerDialog.setView(otpView).create().show()
        }
    }
}