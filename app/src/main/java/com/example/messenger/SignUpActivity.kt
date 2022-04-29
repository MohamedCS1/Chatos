package com.example.messenger

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.messenger.databinding.ActivitySignUpBinding
import com.example.pojo.User
import com.example.tools.LoadingProgress
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity(),TextWatcher {

    lateinit var binding: ActivitySignUpBinding
    private val mAuth:FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
    private val fireStore:FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }
    val  currentUserDocRef get() =  fireStore.document("users/${mAuth.currentUser!!.uid}")

    private val progressDialog by lazy {
        LoadingProgress(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.etEmailOrNumber.addTextChangedListener(this)
        binding.etPassword.addTextChangedListener(this)
        binding.buSignUp.isEnabled = false

        binding.buSignUp.setOnClickListener {
          signUp()
        }

    }

    fun signUp()
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
        createNewAccount(User("","" ,email ,password ,"" ,"" ,"" ,""))
    }

    fun createNewAccount(user: User)
    {
        progressDialog.show()
        mAuth.createUserWithEmailAndPassword(user.email ,user.password).addOnCompleteListener(object :OnCompleteListener<AuthResult>{
            override fun onComplete(task: Task<AuthResult>) {
                if (task.isSuccessful)
                {
                    progressDialog.hide()
                    sendEmailVerification()
                    currentUserDocRef.set(user)
                    val intentToMainActivity = Intent(this@SignUpActivity ,LogInActivity::class.java)
                    intentToMainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intentToMainActivity)
                }
                else
                {
                    progressDialog.hide()
                    binding.tvHintFailure.text = task.exception?.message.toString()
                }
            }
        })
    }

    fun sendEmailVerification() {
        val user = mAuth.currentUser
        user!!.sendEmailVerification().addOnCompleteListener {
            if (it.isSuccessful)
            {
               Toast.makeText(this ,"Email is send" ,Toast.LENGTH_SHORT).show()
            }
            else
            {
                Log.e("email" ,it.exception?.message.toString())
                Toast.makeText(this , it.exception?.message.toString(),Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        if (binding.etEmailOrNumber.text.isNotBlank() && binding.etPassword.text.isNotBlank())
        {
            binding.buSignUp.setCardBackgroundColor(Color.parseColor("#01C5C4"))
            binding.buSignUp.isEnabled = true
        }
        else
        {
            binding.buSignUp.setCardBackgroundColor(Color.parseColor("#27000000"))
            binding.buSignUp.isEnabled = false
        }

        binding.tvHintFailure.text = ""
    }

    override fun afterTextChanged(s: Editable?) {
    }
}