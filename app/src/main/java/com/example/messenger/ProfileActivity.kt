package com.example.messenger

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.Glide.ChatosGlide
import com.example.messenger.databinding.ActivityProfileBinding
import com.example.sharedPreferences.AppSharedPreferences
import com.example.tools.LoadingProgress
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.*

class ProfileActivity : AppCompatActivity() {

    lateinit var binding: ActivityProfileBinding
    lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    lateinit var loadingProgress: LoadingProgress

    private val mAuth:FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
    private val fireStore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    lateinit var appPref:AppSharedPreferences

    val  currentUserDocRef get() =  fireStore.document("users/${mAuth.currentUser!!.uid}")


    private val storageInstance:FirebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }

    private val currentUserStorageRef:StorageReference get() = storageInstance.reference.child(FirebaseAuth.getInstance().currentUser!!.uid)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadingProgress = LoadingProgress(this)

        appPref = AppSharedPreferences()

        appPref.PrefManager(this)

        Toast.makeText(this , appPref.getProfileImagePath() ,Toast.LENGTH_SHORT).show()

        Glide.with(this).load("https://firebasestorage.googleapis.com/v0/b/messenger-d7eaf.appspot.com/o/eTV6cB4jeZUpv8xFwsxvxHlZEso1%2FProfilePictures%2F543e9eef-fb20-378a-916e-7e8ce08a889e?alt=media&token=195375cf-3a06-44e1-9fdd-e4118746aebb").placeholder(R.drawable.ic_shutdown).into(binding.profileImageBig)


        binding.profileImageBig.setOnClickListener {
            val intentImage = Intent().apply {
                type = "image/*"
                action = Intent.ACTION_GET_CONTENT
                putExtra(Intent.EXTRA_MIME_TYPES , arrayOf("image/jpeg" ,"image/png"))
            }

            activityResultLauncher.launch(intentImage)
        }

        binding.buToMain.setOnClickListener {
            onBackPressed()
        }

        activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult() ,object :ActivityResultCallback<ActivityResult>{
                override fun onActivityResult(result: ActivityResult?) {
                    if (result?.resultCode == Activity.RESULT_OK && result.data != null)
                    {
                        binding.profileImageBig.setImageURI(result.data!!.data)

                        val imagePath = result.data!!.data
                        compressImage(imagePath!!)
                    }
                }
            })
    }

    fun compressImage(imageUri:Uri){
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
        val ref = currentUserStorageRef.child("ProfilePictures/${UUID.nameUUIDFromBytes(imageByteArray)}")
        ref.putBytes(imageByteArray).addOnCompleteListener {
            if (it.isSuccessful)
            {
                onSuccess(ref.path)
                loadingProgress.hide()
                Toast.makeText(this ,"Uploading Successfully" ,Toast.LENGTH_SHORT).show()
            }
            else
            {
                loadingProgress.hide()
                Toast.makeText(this , it.exception?.message.toString(),Toast.LENGTH_SHORT).show()
            }
        }
    }
}