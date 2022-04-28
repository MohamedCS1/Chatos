package com.example.messenger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.messenger.databinding.ActivityLoginPhoneNumberBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class LoginPhoneNumberActivity : AppCompatActivity() {

    lateinit var binding:ActivityLoginPhoneNumberBinding

    private val mAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginPhoneNumberBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buNext.setOnClickListener {
            showOtpDialog()
        }
    }

    fun showOtpDialog()
    {
        if (isValidPhoneNumber(binding.etPhoneNumber.text.toString()))
        {
            val alertDialog = AlertDialog.Builder(this)
            val otpView = LayoutInflater.from(this).inflate(R.layout.dialog_otp ,null ,false)
            val otpDialog = alertDialog.setView(otpView).create()
            otpDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            otpDialog.setCancelable(false)
            otpDialog.show()

            val bu_close = otpDialog.findViewById<ImageView>(R.id.bu_close_otp_dialog)
            bu_close?.setOnClickListener {
                otpDialog.hide()
            }

            val tv_phone_number = otpDialog.findViewById<TextView>(R.id.tv_otp_phone_number)
            tv_phone_number?.text = "Enter OTP Code sent +213${binding.etPhoneNumber.text.toString().removeRange(0 ,1)}"
        }
    }

    fun isValidPhoneNumber(phoneNumber:String):Boolean
    {
        if (phoneNumber.length != 10 || phoneNumber[0] != '0' || (phoneNumber[1] != '5' && phoneNumber[1] != '7' && phoneNumber[1] != '6'))
        {
            Toast.makeText(this ,"invalid phone number" ,Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun phoneAuth()
    {
        val phoneNumber = "+2130552938510"
        val smsCode = "123456"

        val firebaseAuthSettings = mAuth.firebaseAuthSettings
        firebaseAuthSettings.setAutoRetrievedSmsCodeForPhoneNumber(phoneNumber, smsCode)

        val options = PhoneAuthOptions.newBuilder(mAuth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {

                    Toast.makeText(this@LoginPhoneNumberActivity ,credential.smsCode.toString() ,Toast.LENGTH_SHORT).show()
                    Log.d("CurrentAuth" ,credential.smsCode.toString())
                }

                override fun onVerificationFailed(p0: FirebaseException) {
                    Toast.makeText(this@LoginPhoneNumberActivity ,p0.message.toString() ,Toast.LENGTH_SHORT).show()
                    Log.d("CurrentAuth" ,p0.message.toString())
                    println(p0.message.toString())

                }

            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }
}