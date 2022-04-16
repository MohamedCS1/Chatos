package com.example.messenger

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.SimpleAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.messenger.databinding.ActivityInfoUserBinding
import com.example.pojo.User
import com.example.sharedPreferences.AppSharedPreferences
import com.example.tools.LoadingProgress
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream

class InfoUserActivity : AppCompatActivity() {

    lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    lateinit var loadingProgress: LoadingProgress

    private val fireStore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    lateinit var appPref: AppSharedPreferences

    private val  currentUserDocRef get() =  fireStore.document("users/${appPref.getCurrentUserUID()}")


    private val storageInstance: FirebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }

    private val currentUserStorageRef: StorageReference
        get() = storageInstance.reference.child(appPref.getCurrentUserUID())

    lateinit var binding:ActivityInfoUserBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInfoUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        appPref = AppSharedPreferences()

        appPref.PrefManager(this)

        loadingProgress = LoadingProgress(this)

        binding.imageViewPhotoProfile.setOnClickListener {
            val intentImage = Intent().apply {
                type = "image/*"
                action = Intent.ACTION_GET_CONTENT
                putExtra(Intent.EXTRA_MIME_TYPES , arrayOf("image/jpeg" ,"image/png"))
            }

            activityResultLauncher.launch(intentImage)
        }

        activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult() ,object :
                ActivityResultCallback<ActivityResult> {
                override fun onActivityResult(result: ActivityResult?) {
                    if (result?.resultCode == Activity.RESULT_OK && result.data != null)
                    {
                        binding.imageViewPhotoProfile.setImageURI(result.data!!.data)

                        val imagePath = result.data!!.data
                        compressImage(imagePath!!)
                    }
                }
            })

        binding.buFinish.setOnClickListener {
            if (binding.etJob.text.isNullOrEmpty())
            {
                binding.tvHintFailure.text = "Please enter your job"
                return@setOnClickListener
            }
            if (binding.spinnerGender.selectedItem.equals("select your gender..."))
            {
                binding.tvHintFailure.text = "Please select your gender"
                return@setOnClickListener
            }
            loadingProgress.show()
            currentUserDocRef.update(mapOf("Job" to binding.etJob.text.toString() ,"gender" to binding.spinnerGender.selectedItem.toString() ,"country" to binding.countryPicker.selectedCountryName.toString())).addOnCompleteListener {
                if (it.isSuccessful)
                {
                    appPref.insertUserJob(binding.etJob.text.toString())
                    appPref.insertUserGender(binding.spinnerGender.selectedItem.toString())
                    appPref.insertUserCountry(binding.countryPicker.selectedCountryName.toString())
                    loadingProgress.hide()
                    startActivity(Intent(this ,MainActivity::class.java))
                    finish()
                }
                else
                {
                    loadingProgress.hide()
                    binding.tvHintFailure.text = it.exception?.message.toString()
                }
            }
        }
    }

    fun compressImage(imageUri: Uri){
        val outputStream = ByteArrayOutputStream()
        MediaStore.Images.Media.getBitmap(this.contentResolver ,imageUri).compress(Bitmap.CompressFormat.JPEG ,30 ,outputStream)
        upLoadProfileImageToFirebase(outputStream.toByteArray())
        {
                path -> currentUserDocRef.update("imagePath" ,path)
            appPref.insertProfileImagePath(path)
        }
    }

    fun upLoadProfileImageToFirebase(imageByteArray:ByteArray ,onSuccess:(imagePath:String) -> Unit)
    {
        loadingProgress.show()
        val ref = currentUserStorageRef.child("ProfilePictures")
        ref.putBytes(imageByteArray).addOnCompleteListener {
            if (it.isSuccessful)
            {
                ref.downloadUrl.addOnCompleteListener {
                        task->
                    if (task.isSuccessful)
                    {
                        onSuccess(task.result.toString())
                        loadingProgress.hide()
                        Toast.makeText(this ,"Uploading Successfully" , Toast.LENGTH_SHORT).show()
                    }
                    else
                    {
                        loadingProgress.hide()
                        Toast.makeText(this , task.exception?.message.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
            }
            else
            {
                loadingProgress.hide()
                Toast.makeText(this , it.exception?.message.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStart() {
        if (appPref.getUserJob() != "" || appPref.getUserGender() != "" || appPref.getUserCountry() != "")
        {
            startActivity(Intent(this ,MainActivity::class.java))
            finish()
        }
        super.onStart()
    }
}