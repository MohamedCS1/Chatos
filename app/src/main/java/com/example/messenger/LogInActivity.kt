package com.example.messenger

import android.content.Intent
import android.content.IntentSender
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.messenger.databinding.ActivityLogInBinding
import com.example.sharedPreferences.AppSharedPreferences
import com.example.tools.LoadingProgress
import com.facebook.*
import com.facebook.appevents.AppEventsLogger
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.*
import java.lang.Exception
import java.util.concurrent.TimeUnit


class LogInActivity : AppCompatActivity() ,TextWatcher{

    private val REQ_ONE_TAP: Int = 2
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest

    lateinit var binding: ActivityLogInBinding
    lateinit var mAuth: FirebaseAuth
    lateinit var appPref: AppSharedPreferences
    lateinit var callbackManager:CallbackManager

    private val progressDialog by lazy {
        LoadingProgress(this)
    }

    private val fireStore:FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    val  currentUserDocRef get() =  fireStore.document("users/${mAuth.currentUser!!.uid}")

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

//        phoneAuth()
        binding.buLogIn.setOnClickListener {
            logInWithEmailAndPassword()
        }

        binding.buLoginGoogle.setOnClickListener {
            googleAuth()
        }

        binding.buCreateNewAccount.setOnClickListener {
            startActivity(Intent(this ,SignUpActivity::class.java))
        }
        binding.buLoginFacebook.setOnClickListener {
            facebookAuth()
        }

        binding.buLoginPhoneNumber.setOnClickListener {
            startActivity(Intent(this ,LoginPhoneNumberActivity::class.java))
        }

    }
    fun logInWithEmailAndPassword()
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
                                        appPref.insertUserEmail(binding.etEmailOrNumber.text.toString())
                                        progressDialog.hide()
                                        val intentToMainActivity = Intent(this@LogInActivity ,InfoUserActivity::class.java)
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

    fun googleAuth()
    {
        progressDialog.show()
        oneTapClient = Identity.getSignInClient(this)
        signInRequest = BeginSignInRequest.builder()
            .setPasswordRequestOptions(BeginSignInRequest.PasswordRequestOptions.builder()
                .setSupported(true)
                .build())
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(getString(com.firebase.ui.auth.R.string.default_web_client_id))
                    .setFilterByAuthorizedAccounts(false)
                    .build())
            .setAutoSelectEnabled(true)
            .build()

        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener(this) { result ->
                try {
                    startIntentSenderForResult(
                        result.pendingIntent.intentSender, REQ_ONE_TAP,
                        null, 0, 0, 0, null)
                } catch (e: IntentSender.SendIntentException) {
                    Toast.makeText(baseContext,"something went wrong try again" ,Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener(this) { e ->
                Toast.makeText(baseContext,e.toString() ,Toast.LENGTH_SHORT).show()
                Log.e("any" ,e.toString())

            }
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

                    Toast.makeText(this@LogInActivity ,credential.smsCode.toString() ,Toast.LENGTH_SHORT).show()
                    Log.d("CurrentAuth" ,credential.smsCode.toString())
                }

                override fun onVerificationFailed(p0: FirebaseException) {
                    Toast.makeText(this@LogInActivity ,p0.message.toString() ,Toast.LENGTH_SHORT).show()
                    Log.d("CurrentAuth" ,p0.message.toString())
                    println(p0.message.toString())

                }

            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun facebookAuth(){

        FacebookSdk.fullyInitialize()
        AppEventsLogger.activateApp(application)

        LoginManager.getInstance().logInWithReadPermissions(this , listOf("email"))

        callbackManager =  CallbackManager.Factory.create()

        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {

            override fun onCancel() {
                progressDialog.hide()
            }

            override fun onError(exception: FacebookException) {
                progressDialog.hide()
                Log.d("CurrentError" ,exception.toString())
            }

            override fun onSuccess(result: LoginResult) {
                Log.d("CurrentError" ,result.toString())
                progressDialog.show()
                handleFacebookToken(result.accessToken)
            }
        })
    }

    fun handleFacebookToken(accessToken: AccessToken)
    {
        val credential = FacebookAuthProvider.getCredential(accessToken.token)
        mAuth.signInWithCredential(credential).addOnCompleteListener {
            progressDialog.show()
            if (it.isSuccessful)
            {
                val user = mAuth.currentUser
                appPref.insertCurrentUserUID(user!!.uid)
                appPref.insertUserEmail(user.email.toString())
                appPref.insertProfileImagePath(user.photoUrl.toString())
                currentUserDocRef.set(
                    com.example.pojo.User(
                        "",
                        user.displayName.toString(),
                        user.email.toString(),
                        "",
                        user.photoUrl.toString(),
                        "",
                        "",
                        "",
                    )
                ).addOnCompleteListener {
                    if (it.isSuccessful)
                    {
                        FirebaseMessaging.getInstance().token.addOnCompleteListener {
                                task->
                            val token = task.result
                            Log.d("token" ,token)
                            fireStore.collection("users").document(appPref.getCurrentUserUID()).update(mapOf("token" to(token)))
                                .addOnCompleteListener {
                                    if (!it.isSuccessful)
                                    {
                                        appPref.insertProfileImagePath(user.email.toString())
                                        progressDialog.hide()
                                    }
                                    else
                                    {
                                        progressDialog.hide()
                                        val intentToMainActivity = Intent(this@LogInActivity ,InfoUserActivity::class.java)
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
                        Toast.makeText(baseContext,"${it.exception!!.message}" ,Toast.LENGTH_LONG).show()
                    }
                }
            }
            else
            {
                progressDialog.hide()
                Toast.makeText(baseContext,"${it.exception!!.message}" ,Toast.LENGTH_LONG).show()
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


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQ_ONE_TAP -> {
                try {
                    val credential = oneTapClient.getSignInCredentialFromIntent(data)
                    val idToken = credential.googleIdToken
                    val username = credential.displayName
                    val password = credential.password
                    val photoProfile = credential.profilePictureUri
                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)

                    appPref.insertProfileImagePath(photoProfile.toString())
                    mAuth.signInWithCredential(firebaseCredential)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                val currentUser = mAuth.currentUser
                                appPref.insertCurrentUserUID(currentUser!!.uid)
                                currentUserDocRef.set(
                                    com.example.pojo.User(
                                        currentUser.uid,
                                        username.toString(),
                                        "By facebook",
                                        "",
                                        photoProfile.toString(),
                                        "",
                                        "",
                                        "",
                                    )
                                ).addOnCompleteListener {
                                    if (it.isSuccessful)
                                    {
                                        progressDialog.show()
                                        val intentToMainActivity = Intent(this@LogInActivity ,InfoUserActivity::class.java)
                                        intentToMainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                        startActivity(intentToMainActivity)
                                        finish()
                                    }
                                    else
                                    {
                                        progressDialog.show()
                                        Toast.makeText(baseContext,
                                            it.exception!!.message.toString(),Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                progressDialog.show()
                                Toast.makeText(baseContext,
                                    task.exception!!.message.toString(),Toast.LENGTH_SHORT).show()
                            }
                        }
                    Log.d("any" ,"$idToken / $username / $password")
                } catch (e: ApiException) {
                    progressDialog.show()
                    Toast.makeText(baseContext,"something went wrong try again" ,Toast.LENGTH_SHORT).show()
                }
            }
        }
        try {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }catch (ex:Exception)
        {
         Log.d("LoginActivity" ,ex.toString())
        }
    }



    override fun onStart() {

        if (appPref.getCurrentUserUID().isNotEmpty() && appPref.getCurrentUserUID().isNotBlank())
        {
            val intentToMainActivity = Intent(this@LogInActivity ,InfoUserActivity::class.java)
            intentToMainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intentToMainActivity)
            finish()
        }
        super.onStart()
    }
}