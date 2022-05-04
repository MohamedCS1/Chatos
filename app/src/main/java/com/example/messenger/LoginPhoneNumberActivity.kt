package com.example.messenger

import android.annotation.SuppressLint
import android.content.Intent
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Chronometer
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.compose.ui.graphics.Color
import com.chaos.view.PinView
import com.example.messenger.databinding.ActivityLoginPhoneNumberBinding
import com.example.pojo.User
import com.example.sharedPreferences.AppSharedPreferences
import com.example.tools.LoadingProgress
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.messaging.FirebaseMessaging
import java.lang.Exception
import java.util.concurrent.TimeUnit

class LoginPhoneNumberActivity : AppCompatActivity() {

    lateinit var binding:ActivityLoginPhoneNumberBinding

    private val fireStore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    lateinit var appPref: AppSharedPreferences

    val  currentUserDocRef get() =  fireStore.document("users/${mAuth.currentUser!!.uid}")

    private val progressDialog by lazy {
        LoadingProgress(this)
    }
    private val mAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    var mVerificationID: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginPhoneNumberBinding.inflate(layoutInflater)
        setContentView(binding.root)


        appPref = AppSharedPreferences()
        appPref.PrefManager(this)

        binding.buNext.setOnClickListener {
            showOtpDialog()
        }
    }

    @SuppressLint("SetTextI18n")
    fun showOtpDialog()
    {

        if (isValidPhoneNumber(binding.etPhoneNumber.text.toString()))
        {
            var counter = 60
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
                    Toast.makeText(this ,"invalid otp" ,Toast.LENGTH_LONG).show()
                }
            }


            val chronometer_resend = otpDialog.findViewById<Chronometer>(R.id.chronometer_resend)

            chronometer_resend!!.start()

            chronometer_resend.setOnChronometerTickListener {
                counter --
                chronometer_resend.text = "0:$counter"
                if (counter == 0)
                {
                    counter = 60
                    phoneAuth(binding.etPhoneNumber.text.toString())
                }
            }
        }
    }

    fun isValidPhoneNumber(phoneNumber:String):Boolean
    {
        if (phoneNumber.length != 10 || phoneNumber[0] != '0' || (phoneNumber[1] != '5' && phoneNumber[1] != '7' && phoneNumber[1] != '6'))
        {
            Toast.makeText(this ,"invalid phone number" ,Toast.LENGTH_LONG).show()
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
                }

                override fun onVerificationFailed(p0: FirebaseException) {
                    Toast.makeText(this@LoginPhoneNumberActivity ,p0.message.toString() ,Toast.LENGTH_LONG).show()
                    println(p0.message.toString())

                }

                override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
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
                val user = mAuth.currentUser
                currentUserDocRef.get().addOnSuccessListener {
                        document->

                    if (document.exists())
                    {

                        currentUserDocRef.update(
                            mapOf("email" to user?.email
                                ,"name" to user?.displayName
                                ,"uid" to user?.uid
                                ,"imagePath" to user?.photoUrl.toString())
                        ).addOnCompleteListener {
                            if (it.isSuccessful)
                            {
                                insertTokenToFirebase()
                                dataUserfromFirebaseToAppPref()
                            }
                            else
                            {
                                progressDialog.hide()
                                Toast.makeText(baseContext,"${it.exception!!.message}" ,Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                    else
                    {

                        currentUserDocRef.set(
                            mapOf("email" to user?.email
                                ,"name" to user?.displayName
                                ,"uid" to user?.uid
                                ,"imagePath" to user?.photoUrl.toString())

                        ).addOnCompleteListener {
                            if (it.isSuccessful)
                            {
                                insertTokenToFirebase()
                                dataUserfromFirebaseToAppPref()
                            }
                            else
                            {
                                progressDialog.hide()
                                Toast.makeText(baseContext,"${it.exception!!.message}" ,Toast.LENGTH_LONG).show()
                            }
                        }
                    }

                }
            }
            else
            {
                Toast.makeText(this@LoginPhoneNumberActivity ,
                    it.exception!!.message.toString() ,Toast.LENGTH_LONG).show()
            }
        }
    }

    fun dataUserfromFirebaseToAppPref()
    {
        currentUserDocRef.addSnapshotListener(object : EventListener<DocumentSnapshot>
        {
            override fun onEvent(
                value: DocumentSnapshot?,
                error: FirebaseFirestoreException?
            ) {
                if (error != null)
                {
                    progressDialog.hide()
                    Toast.makeText(this@LoginPhoneNumberActivity ,"something wrong please try again" ,Toast.LENGTH_LONG).show()
                }
                else
                {
                    try {
                        val user = value?.toObject(User::class.java)
                        appPref.insertUserJob(user!!.job)
                        appPref.insertUserGender(user.gender)
                        appPref.insertUserCountry(user.country)
                        appPref.insertUserEmail(user.email)
                        appPref.insertProfileImagePath(user.imagePath)
                        appPref.insertCurrentUserUID(user.uid)
                    }catch (ex: Exception)
                    {
                        Log.e("current Exception" ,ex.message.toString())
                        Toast.makeText(this@LoginPhoneNumberActivity ,ex.message.toString() ,Toast.LENGTH_LONG).show()
                    }
                    intentToInfoActivity()
                }

            }
        })
    }

    fun insertTokenToFirebase()
    {
        FirebaseMessaging.getInstance().token.addOnCompleteListener {
                task->
            val token = task.result
            Log.d("token" ,token)
            fireStore.collection("users").document(mAuth.currentUser!!.uid).update(mapOf("token" to(token)))
                .addOnCompleteListener {
                    if (!it.isSuccessful)
                    {
                        progressDialog.hide()
                        Toast.makeText(baseContext,"something went wrong try again" ,Toast.LENGTH_LONG).show()

                    }
                    else
                    {
                        progressDialog.hide()
                        intentToInfoActivity()
                    }
                }
        }.addOnFailureListener {
            Toast.makeText(baseContext,"something went wrong try again" ,Toast.LENGTH_LONG).show()
        }
    }
    fun intentToInfoActivity()
    {
        val intentToMainActivity = Intent(this@LoginPhoneNumberActivity ,InfoUserActivity::class.java)
        intentToMainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intentToMainActivity)
        finish()
    }
}