package com.example.camera

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
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
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.io.IOException
import java.net.URI
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

    private var clicked = false;
    private val REQUEST_IMAGE_CAPTURE = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val add = findViewById<FloatingActionButton>(R.id.fab_add)
        val capture = findViewById<FloatingActionButton>(R.id.fab_pic)
        val upload = findViewById<FloatingActionButton>(R.id.fab_upload)

        add.setOnClickListener{
            setVisibility(clicked)
            setAnimation(clicked)

            (!clicked).also { clicked = it }
        }
        capture.setOnClickListener{
//            Toast.makeText(this, "Capture a Picture", Toast.LENGTH_SHORT).show()
            dispatchTakePictureIntent()

        }
        upload.setOnClickListener{
            setPictureInApp()
        }

    }

    private fun setVisibility(clicked: Boolean) {
        val capture = findViewById<FloatingActionButton>(R.id.fab_pic)
        val upload = findViewById<FloatingActionButton>(R.id.fab_upload)
        if(!clicked) {
            capture.visibility = View.VISIBLE
            upload.visibility= View.VISIBLE
        }
         else{
            capture.visibility = View.INVISIBLE
            upload.visibility = View.INVISIBLE
        }
    }

    private fun setAnimation(clicked: Boolean){
        val add = findViewById<FloatingActionButton>(R.id.fab_add)
        val capture = findViewById<FloatingActionButton>(R.id.fab_pic)
        val upload = findViewById<FloatingActionButton>(R.id.fab_upload)
        if (!clicked){
            upload.startAnimation(fromBottom)
            capture.startAnimation(fromBottom)
            add.startAnimation(rotateOpen)
        }else{
            upload.startAnimation(toBottom)
            capture.startAnimation(toBottom)
            add.startAnimation(rotateClose)
        }
    }

    private fun setPictureInApp() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } catch (e: ActivityNotFoundException) {
            // display error state to the user
        }
    }


    @SuppressLint("QueryPermissionsNeeded")
    private fun dispatchTakePictureIntent(){
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                val photoFile : File? = try {
                    createImageFile()
                }catch (ex: IOException){null}
                photoFile?.also{
                    val photoURI: Uri? = FileProvider.getUriForFile(
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

    private lateinit var currentPhotoPath: String

    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir : File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_$()timeStamp_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            val imageView = findViewById<ImageView>(R.id.picture)
            imageView.setImageBitmap(imageBitmap)
        }
    }
}