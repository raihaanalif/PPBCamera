package com.example.camera

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.io.IOException
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val add = findViewById<FloatingActionButton>(R.id.fab_add)
        val capture = findViewById<FloatingActionButton>(R.id.fab_pic)
        val upload = findViewById<FloatingActionButton>(R.id.fab_upload)
        val img = findViewById<FloatingActionButton>(R.id.fab_galery)

        add.setOnClickListener{
            setVisibility(clicked)
            setAnimation(clicked)

            (!clicked).also { clicked = it }
        }
        capture.setOnClickListener{
//          Toast.makeText(this, "Capture a Picture", Toast.LENGTH_SHORT).show()
            dispatchTakePictureIntent()


        }
        upload.setOnClickListener{
            setPictureInApp()
        }
        img.setOnClickListener{
            galleryAddPic()
        }

    }


    private fun setVisibility(clicked: Boolean) {
        val capture = findViewById<FloatingActionButton>(R.id.fab_pic)
        val upload = findViewById<FloatingActionButton>(R.id.fab_upload)
        val img = findViewById<FloatingActionButton>(R.id.fab_galery)
        if(!clicked) {
            capture.visibility = View.VISIBLE
            upload.visibility= View.VISIBLE
            img.visibility = View.VISIBLE
        }
         else{
            capture.visibility = View.INVISIBLE
            upload.visibility = View.INVISIBLE
            img.visibility = View.INVISIBLE
        }
    }

    private fun setAnimation(clicked: Boolean){
        val add = findViewById<FloatingActionButton>(R.id.fab_add)
        val capture = findViewById<FloatingActionButton>(R.id.fab_pic)
        val upload = findViewById<FloatingActionButton>(R.id.fab_upload)
        val img = findViewById<FloatingActionButton>(R.id.fab_galery)
        if (!clicked){
            upload.startAnimation(fromBottom)
            capture.startAnimation(fromBottom)
            img.startAnimation(fromBottom)
            add.startAnimation(rotateOpen)
        }else{
            upload.startAnimation(toBottom)
            capture.startAnimation(toBottom)
            img.startAnimation(toBottom)
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


    private fun dispatchTakePictureIntent(){
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                0
            )
        } else {
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                takePictureIntent.resolveActivity(packageManager)?.also {
                    val photoFile: File? = try {
                        createImageFile()
                    } catch (ex: IOException) {
                        null
                    }
                    photoFile?.also {
                        val photoURI: Uri = FileProvider.getUriForFile(
                            this,
                            "com.example.camera.provider",
                            it
                        )
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                    }
                }
            }
        }
    }

    private lateinit var currentPhotoPath: String

    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            timeStamp,
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val imageView = findViewById<ImageView>(R.id.picture)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            imageView.setImageBitmap(imageBitmap)
        }else if(requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK){
            imageView.setImageURI(data?.data)
        }
    }

    private fun galleryAddPic() {
        val galleryPictureIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        galleryPictureIntent.type = "image/*"
        startActivityForResult(Intent.createChooser(galleryPictureIntent, "Select Picture"), REQUEST_IMAGE_PICK)
    }
}