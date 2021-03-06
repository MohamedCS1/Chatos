package com.example.messenger

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.example.messenger.databinding.ActivityProfileBinding
import com.example.pojo.ImageMessage
import com.example.sharedPreferences.AppSharedPreferences
import com.example.tools.DialogMethod
import com.example.tools.DialogMethodClick
import com.example.tools.LoadingProgress
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
import java.io.File
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

    private val IMAGE_CPTURE_REQUEST_CODE = 94

    lateinit var dialogMethod: DialogMethod

    lateinit var currentPhotoPath:String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        appPref = AppSharedPreferences()

        appPref.PrefManager(this)

        loadingProgress = LoadingProgress(this)

        dialogMethod = DialogMethod(this)

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
            dialogMethod.show()
            dialogMethod.setOnMethodClick(object: DialogMethodClick {
                override fun onClickDialogMethod(id: Int) {
                    if (id == R.id.bu_storage)
                    {
                        val intentImage = Intent().apply {
                            type = "image/*"
                            action = Intent.ACTION_GET_CONTENT
                            putExtra(Intent.EXTRA_MIME_TYPES , arrayOf("image/jpeg" ,"image/png"))
                        }
                        activityResultLauncher.launch(intentImage)
                    }
                    else if (id == R.id.bu_camera)
                    {

                        val fileName = "photo"
                        val storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                        val imageFile = File.createTempFile(fileName ,".jpg" ,storageDirectory)

                        currentPhotoPath = imageFile.absolutePath
                        val imageUri = FileProvider.getUriForFile(this@ProfileActivity ,"com.example.messenger.fileprovider" ,imageFile)
                        val intentToCamera = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        intentToCamera.putExtra(MediaStore.EXTRA_OUTPUT ,imageUri)

                        startActivityForResult(intentToCamera ,IMAGE_CPTURE_REQUEST_CODE)
                    }
                }
            })
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
                        compressImageUri(imagePath!!)
                    }
                }
            })
    }

    fun retrieveImageFromStorage()
    {
        Glide.with(this).load(appPref.getProfileImagePath()).placeholder(R.drawable.ic_photo_placeholder).into(binding.profileImageBig)
    }

    fun compressImageUri(imageUri:Uri){
        val outputStream = ByteArrayOutputStream()
        MediaStore.Images.Media.getBitmap(this.contentResolver ,imageUri).compress(Bitmap.CompressFormat.JPEG ,30 ,outputStream)
        upLoadProfileImageToFirebase(outputStream.toByteArray())
        {
            path -> currentUserDocRef.update("imagePath" ,path)
            appPref.insertProfileImagePath(path)
        }
    }

    fun compressImageBitmap(imageBitmap: Bitmap){

        val outputStream = ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 30, outputStream)
        val byteArray = outputStream.toByteArray()

        upLoadProfileImageToFirebase(byteArray)
        {
            path ->  currentUserDocRef.update("imagePath" ,path)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == IMAGE_CPTURE_REQUEST_CODE)
        {
            val imageBitmap = BitmapFactory.decodeFile(currentPhotoPath)
            compressImageBitmap(imageBitmap)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

}