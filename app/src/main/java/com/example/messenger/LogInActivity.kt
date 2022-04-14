package com.example.messenger

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import com.example.messenger.databinding.ActivityLogInBinding
import com.example.sharedPreferences.AppSharedPreferences
import com.example.tools.LoadingProgress
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.*

class LogInActivity : AppCompatActivity() ,TextWatcher{

    lateinit var binding: ActivityLogInBinding
    lateinit var mAuth: FirebaseAuth
    lateinit var appPref: AppSharedPreferences

    private val progressDialog by lazy {
        LoadingProgress(this)
    }

    private val fireStore:FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        appPref = AppSharedPreferences()
        appPref.PrefManager(this)

        binding.etEmailOrNumber.addTextChangedListener(this)
        binding.etPassword.addTextChangedListener(this)
        binding.buLogIn.isEnabled = false

        mAuth = FirebaseAuth.getInstance()

        binding.buLogIn.setOnClickListener {
            logIn()
        }

        binding.buCreateNewAccount.setOnClickListener {
            startActivity(Intent(this ,SignUpActivity::class.java))
        }

    }

    fun logIn()
    {
        val email = binding.etEmailOrNumber.text.toString().trim()
        val password = binding.etPassword.text.toString()



        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            binding.etEmailOrNumber.error = "Please enter a valid email"
            binding.etEmailOrNumber.requestFocus()
            return
        }

        if (password.length < 6)
        {
            binding.etPassword.error = "Password 6 char required"
            binding.etPassword.requestFocus()
            return
        }
        signInWithEmailAndPassword(email ,password)



    }

    private fun signInWithEmailAndPassword(email: String, password: String) {
        progressDialog.show()
        mAuth.signInWithEmailAndPassword(email ,password).addOnCompleteListener {
            if(it.isSuccessful)
            {
                emailIsVerify()
            }
            else
            {
                progressDialog.hide()
                binding.tvHintFailure.text = "${it.exception!!.message}"
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun emailIsVerify()
    {
       GlobalScope.launch(Dispatchers.Main) {

           progressDialog.show()

           val user = mAuth.currentUser

           val reloadFirebase = async { user!!.reload() }

           reloadFirebase.await().addOnCompleteListener {
               if (it.isSuccessful)
               {
                   if (user!!.isEmailVerified)
                   {
                       appPref.insertCurrentUserUID(user.uid)
                       FirebaseMessaging.getInstance().token.addOnCompleteListener {
                           task->
                           if (!task.isSuccessful)
                           {
                               binding.tvHintFailure.text = "Something went wrong. Please try again."
                               progressDialog.hide()
                               return@addOnCompleteListener
                           }
                           val token = task.result
                           Log.d("token" ,token)
                           fireStore.collection("users").document(appPref.getCurrentUserUID()).update(mapOf("token" to(token)))
                               .addOnCompleteListener {
                                   if (!it.isSuccessful)
                                   {
                                       binding.tvHintFailure.text = "Something went wrong. Please try again."
                                       progressDialog.hide()
                                   }
                                   else
                                   {

                                       progressDialog.hide()
                                       val intentToMainActivity = Intent(this@LogInActivity ,MainActivity::class.java)
                                       intentToMainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                       startActivity(intentToMainActivity)
                                       finish()
                                   }
                               }
                       }

                   }
                   else
                   {
                       progressDialog.hide()
                       binding.tvHintFailure.text = "Please check your email to verify it"
                   }
               }
               else
               {
                   progressDialog.hide()
                   binding.tvHintFailure.text = "Please check your internet"
               }
           }
       }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        if (binding.etEmailOrNumber.text.isNotBlank() && binding.etPassword.text.isNotBlank())
        {
            binding.buLogIn.setCardBackgroundColor(Color.parseColor("#01C5C4"))
            binding.buLogIn.isEnabled = true
        }
        else
        {
            binding.buLogIn.setCardBackgroundColor(Color.parseColor("#27000000"))
            binding.buLogIn.isEnabled = false
        }

        binding.tvHintFailure.text = ""
    }

    override fun afterTextChanged(s: Editable?) {
    }

    override fun onStart() {

        if (appPref.getCurrentUserUID().isNotEmpty() && appPref.getCurrentUserUID().isNotBlank())
        {
            val intentToMainActivity = Intent(this@LogInActivity ,MainActivity::class.java)
            intentToMainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intentToMainActivity)
            finish()
        }
        super.onStart()
    }
}