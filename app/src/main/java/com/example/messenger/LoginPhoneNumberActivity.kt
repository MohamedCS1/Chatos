package com.example.messenger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import com.chaos.view.PinView
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

    var mVerificationID: String = ""

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


            phoneAuth(binding.etPhoneNumber.text.toString())

            val bu_close = otpDialog.findViewById<ImageView>(R.id.bu_close_otp_dialog)
            bu_close?.setOnClickListener {
                otpDialog.hide()
            }

            val tv_phone_number = otpDialog.findViewById<TextView>(R.id.tv_otp_phone_number)
            tv_phone_number?.text = "Enter OTP Code sent +213${binding.etPhoneNumber.text.toString().removeRange(0 ,1)}"

            val bu_verification = otpDialog.findViewById<CardView>(R.id.bu_verification)
            val otpPinView = otpDialog.findViewById<PinView>(R.id.otpPinView)
            bu_verification?.setOnClickListener {
                if (mVerificationID.isNotEmpty() && otpPinView!!.text!!.length > 5)
                {
                    otpVerification(mVerificationID ,otpPinView.text.toString())
                }
                else
                {
                    Toast.makeText(this ,"invalid otp" ,Toast.LENGTH_SHORT).show()
                }
            }
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

    private fun phoneAuth(phoneNumber:String)
    {
        val smsCode = "123456"
        val phoneNumberFirebaseAuthSettingsFormat = phoneNumber

        val firebaseAuthSettings = mAuth.firebaseAuthSettings
        firebaseAuthSettings.setAutoRetrievedSmsCodeForPhoneNumber("+213$phoneNumberFirebaseAuthSettingsFormat", smsCode)

        val options = PhoneAuthOptions.newBuilder(mAuth)
            .setPhoneNumber("+213${phoneNumber.removeRange(0,1)}")
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    Log.d("CurrentAuth" ,"credential -->"+credential.smsCode.toString())
                }

                override fun onVerificationFailed(p0: FirebaseException) {
                    Toast.makeText(this@LoginPhoneNumberActivity ,p0.message.toString() ,Toast.LENGTH_SHORT).show()
                    Log.d("CurrentAuth" ,"VerificationFailed -->"+p0.message.toString())
                    println(p0.message.toString())

                }

                override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
//                    Toast.makeText(this@LoginPhoneNumberActivity ,p0 ,Toast.LENGTH_SHORT).show()
                    Log.d("CurrentAuth" ,"CodeSent -->"+p0)

                    super.onCodeSent(p0, p1)
                    mVerificationID = p0
                }

            }).build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun otpVerification(id: String ,otpCode:String)
    {
        val credential = PhoneAuthProvider.getCredential(id ,otpCode)

        mAuth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful)
            {
                val user = it.result.user
                Toast.makeText(this ,"Verification passed" ,Toast.LENGTH_SHORT).show()
                Log.d("CurrentAuth" ,user.toString())
            }
            else
            {
                Toast.makeText(this@LoginPhoneNumberActivity ,
                    it.exception!!.message.toString() ,Toast.LENGTH_SHORT).show()
            }
        }
    }
}