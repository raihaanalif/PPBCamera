package com.example.camera

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.icu.text.CaseMap
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.camera.databinding.ActivityMainBinding
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private val rotateOpen: Animation by lazy {
        AnimationUtils.loadAnimation(
            this,
            R.anim.rotate_open_anim
        )
    }
    private val rotateClose: Animation by lazy {
        AnimationUtils.loadAnimation(
            this,
            R.anim.rotate_close_anim
        )
    }
    private val fromBottom: Animation by lazy {
        AnimationUtils.loadAnimation(
            this,
            R.anim.from_bot_anim
        )
    }
    private val toBottom: Animation by lazy {
        AnimationUtils.loadAnimation(
            this,
            R.anim.to_bot_anim
        )
    }
    private var clicked = false
    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_IMAGE_PICK = 2
    private val REQUEST_TAKE_PICTURE = 3
    private var currentPhotoPath=""
    private val FILE_NAME = "photo.jpg"
    private var filePath: Uri? = null
    private var firebaseStore: FirebaseStorage? = null
    private var storageReferences: StorageReference? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val add = findViewById<FloatingActionButton>(R.id.fab_add)
        val capture = findViewById<FloatingActionButton>(R.id.fab_pic)
        val upload = findViewById<FloatingActionButton>(R.id.fab_upload)
        val img = findViewById<FloatingActionButton>(R.id.fab_galery)
//        val save = findViewById<FloatingActionButton>(R.id.fab_save)

        firebaseStore = FirebaseStorage.getInstance()
        storageReferences = FirebaseStorage.getInstance().reference

        add.setOnClickListener{
            setVisibility(clicked)
            setAnimation(clicked)

            (!clicked).also { clicked = it }
        }
        capture.setOnClickListener{
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                    val permission = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    requestPermissions(permission, 101)
                }
                else{
                    dispatchTakePictureIntent()
                }
            }
        }
        upload.setOnClickListener{
            setPictureInApp()
        }
        img.setOnClickListener{
            galleryAddPic()
        }
//        save.setOnClickListener{
//            savePicture()
//        }
    }

    private fun setVisibility(clicked: Boolean) {
        val capture = findViewById<FloatingActionButton>(R.id.fab_pic)
        val upload = findViewById<FloatingActionButton>(R.id.fab_upload)
        val img = findViewById<FloatingActionButton>(R.id.fab_galery)
//        val save = findViewById<FloatingActionButton>(R.id.fab_save)
        if(!clicked) {
            capture.visibility = View.VISIBLE
            upload.visibility= View.VISIBLE
            img.visibility = View.VISIBLE
//            save.visibility = View.VISIBLE
        }
         else{
            capture.visibility = View.INVISIBLE
            upload.visibility = View.INVISIBLE
            img.visibility = View.INVISIBLE
//            save.visibility = View.INVISIBLE
        }
    }

    private fun setAnimation(clicked: Boolean){
        val add = findViewById<FloatingActionButton>(R.id.fab_add)
        val capture = findViewById<FloatingActionButton>(R.id.fab_pic)
        val upload = findViewById<FloatingActionButton>(R.id.fab_upload)
        val img = findViewById<FloatingActionButton>(R.id.fab_galery)
//        val save = findViewById<FloatingActionButton>(R.id.fab_save)
        if (!clicked){
            upload.startAnimation(fromBottom)
            capture.startAnimation(fromBottom)
            img.startAnimation(fromBottom)
//            save.startAnimation(fromBottom)

            add.startAnimation(rotateOpen)
        }else{
            upload.startAnimation(toBottom)
            capture.startAnimation(toBottom)
            img.startAnimation(toBottom)
//            save.startAnimation(toBottom)

            add.startAnimation(rotateClose)
        }
    }

    private fun setPictureInApp() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun dispatchTakePictureIntent() {
//        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
//            takePictureIntent.resolveActivity(packageManager)?.also {
//                val photoFile: File? = try {
//                    createImageFile()
//                } catch (ex: IOException) {
//                    null
//                }
//                photoFile?.also {
//                    val photoURI: Uri = FileProvider.getUriForFile(
//                        this,
//                        "com.example.camera.fileprovider",
//                        it
//                    )
//                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
//                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PICTURE)
//                }
//            }
//        }
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val filePhoto = getPhotoFile(FILE_NAME)
        val providerFile = FileProvider.getUriForFile(this, "com.example.camera.provider", filePhoto)
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, providerFile)
        if(takePictureIntent.resolveActivity(packageManager) != null){
            startActivityForResult(takePictureIntent, REQUEST_TAKE_PICTURE)
            Toast.makeText(this, "Photo's Capture", Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(this, "Camera couldn't Open", Toast.LENGTH_SHORT).show()
        }
    }

//    @SuppressLint("SimpleDateFormat")
//    @Throws(IOException::class)
//    private fun createImageFile(): File {
//        val timeStamp = SimpleDateFormat("yyyyMMddHHmmss").format(Date())
//        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
//        return File.createTempFile(
//            "JPEG_" + timeStamp + "_",
//            ".jpg",
//            storageDir
//        ).apply {
//            currentPhotoPath = absolutePath
//        }
//    }

    private fun getPhotoFile(fileName: String):File {
        val directoryStorage = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, ".jpg", directoryStorage)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val imageView = findViewById<ImageView>(R.id.picture)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            imageView.setImageBitmap(imageBitmap)
        }else if(requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK){
            if(data != null){
                filePath = data.data
            }
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, filePath)
            uploadImage()
            imageView.setImageBitmap(bitmap)
        }
    }

    private fun galleryAddPic() {
        val galleryPictureIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        galleryPictureIntent.type = "image/*"
        startActivityForResult(Intent.createChooser(galleryPictureIntent, "Select Picture"), REQUEST_IMAGE_PICK)
    }

    private fun uploadImage(){
        if(filePath != null){
            val ref = storageReferences?.child("uploads/" + UUID.randomUUID().toString())
            val uploadTask = ref?.putFile(filePath!!)
            val urlTask = uploadTask?.continueWithTask(Continuation { task ->
                if (!task.isSuccessful){
                    task.exception?.let { throw it }
                }
                return@Continuation ref.downloadUrl
            })?.addOnCompleteListener{ task ->
                if(task.isSuccessful){
                    val downloadUri = task.result
                    addUploadRecordToDb(downloadUri.toString())
                    Toast.makeText(this, "Saved Photos is Success", Toast.LENGTH_SHORT).show()
                }
            }?.addOnFailureListener{}
        }else{ Toast.makeText(this, "Please Upload an Image", Toast.LENGTH_SHORT).show() }
    }

    private fun addUploadRecordToDb(uri: String){
        val db = FirebaseFirestore.getInstance()

        val data = HashMap<String, Any>()
        data["imageUri"] = uri

        db.collection("posts")
            .add(data)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(this, "Saved to DataBase", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener{ e ->
                Toast.makeText(this, "Error saving to DataBase", Toast.LENGTH_SHORT).show()
            }
    }
//    @Throws(IOException::class)
//    private fun savePicture() {
//        try {
//            Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { galleryIntent ->
//                val f = File(currentPhotoPath)
//                galleryIntent.data = Uri.fromFile(f)
//                sendBroadcast(galleryIntent)
//                Toast.makeText(this, "This Picture is Saved", Toast.LENGTH_SHORT).show()
//            }
//        }catch (e: Exception){
//            Toast.makeText(this, "This Picture Failed to Saved", Toast.LENGTH_SHORT).show()
//        }
//
//    }
}