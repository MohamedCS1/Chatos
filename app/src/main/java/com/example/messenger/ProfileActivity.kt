package com.example.messenger

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import com.bumptech.glide.Glide
import com.example.messenger.databinding.ActivityProfileBinding
import com.example.sharedPreferences.AppSharedPreferences
import com.example.tools.LoadingProgress
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
import java.util.*

class ProfileActivity : AppCompatActivity() {

    lateinit var binding: ActivityProfileBinding
    lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    lateinit var loadingProgress: LoadingProgress

    private val fireStore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    lateinit var appPref:AppSharedPreferences

    val  currentUserDocRef get() =  fireStore.document("users/${appPref.getCurrentUserUID()}")


    private val storageInstance:FirebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }

    private val currentUserStorageRef:StorageReference get() = storageInstance.reference.child(FirebaseAuth.getInstance().currentUser!!.uid)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        appPref = AppSharedPreferences()

        appPref.PrefManager(this)

        loadingProgress = LoadingProgress(this)

        retrieveImageFromStorage()

        binding.tvUserJob.text = appPref.getUserJob()
        binding.tvUserName.text = appPref.getCurrentUserName()
        binding.tvUserEmail.text = appPref.getUserEmail()

        binding.buLogOut.setOnClickListener {

            val dialogSingout = AlertDialog.Builder(this)
            val view = View.inflate(this ,R.layout.dialog_singout , null)
            val createDialog = dialogSingout.setView(view).create()
            createDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            createDialog.show()
            val bu_yes = createDialog.findViewById<Button>(R.id.bu_yes)
            val bu_no = createDialog.findViewById<Button>(R.id.bu_no)

            bu_yes.setOnClickListener {
                appPref.clearSession()
                FirebaseAuth.getInstance().signOut()
                val intentToLoginActivity = Intent(this ,LogInActivity::class.java)
                intentToLoginActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intentToLoginActivity)
                finish()
            }

            bu_no.setOnClickListener{
                createDialog.dismiss()
            }
        }

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

    fun retrieveImageFromStorage()
    {
        Glide.with(this).load(appPref.getProfileImagePath()).placeholder(R.drawable.ic_photo_placeholder).into(binding.profileImageBig)
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
                        Toast.makeText(this ,"Uploading Successfully" ,Toast.LENGTH_SHORT).show()
                    }
                    else
                    {
                        loadingProgress.hide()
                        Toast.makeText(this , task.exception?.message.toString(),Toast.LENGTH_SHORT).show()
                    }
                }
            }
            else
            {
                loadingProgress.hide()
                Toast.makeText(this , it.exception?.message.toString(),Toast.LENGTH_SHORT).show()
            }
        }
    }
}