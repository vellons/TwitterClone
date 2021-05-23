package it.uninsubria.pdm.vellons.twitterclone

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class NewTweetActivity : AppCompatActivity() {
    private val TAG = "NewTweetActivity"
    private val REQUEST_IMAGE_CAPTURE = 1001
    private val REQUEST_PICK_IMAGE = 1002
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var currentPhotoPath: String  // Used to save photo taken
    private lateinit var imageViewCreateTweetImage: ImageView
    private var isTweetWithPhoto = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_tweet)
        auth = Firebase.auth
        firestore = Firebase.firestore
        storage = Firebase.storage

        imageViewCreateTweetImage = findViewById(R.id.imageViewCreateTweetImage)
        val imageViewProfileUserImage: ShapeableImageView =
            findViewById(R.id.imageViewProfileUserImage)
        val editTextNewTweetText: EditText = findViewById(R.id.editTextNewTweetText)
        val buttonPostTweet: Button = findViewById(R.id.buttonPostTweet)
        imageViewCreateTweetImage.visibility = View.GONE // Hide tweet image

        editTextNewTweetText.requestFocus() // Set focus to open keyboard

        // Get user photo if present
        val uid = auth.currentUser?.uid
        val userRef = firestore.collection("users").document(uid!!)
        userRef.get().addOnSuccessListener { document ->
            if (document != null) {
                val profilePath = document.getString("photo")
                if (profilePath != null) { // Download profile photo if present
                    val storageRef = storage.reference
                    val profilePhotoRef = storageRef.child(profilePath)

                    val ONE_MEGABYTE: Long = 1024 * 1024
                    // .getBytes take as a parameter the maximum download size accepted
                    profilePhotoRef.getBytes(8 * ONE_MEGABYTE)
                        .addOnSuccessListener { bytes ->
                            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            imageViewProfileUserImage.setImageBitmap(bmp)
                        }.addOnFailureListener {
                            Log.d(TAG, "Failed to download user image ", it)
                        }
                }
            } else {
                Log.d(TAG, "No such document. User do not exists in DB")
            }
        }.addOnFailureListener { exception ->
            Log.d(TAG, "Failed to get user info ", exception)
        }


        buttonPostTweet.setOnClickListener { // Button post tweet clicked
            displayToast(R.string.not_implemented_yet)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Log.d(TAG, "Photo taken. PATH:  $currentPhotoPath")
            if (data?.extras?.get("data") != null) { // Used to get the thumbnail
                val imageBitmap = data.extras?.get("data") as Bitmap
                imageViewCreateTweetImage.visibility = View.VISIBLE
                imageViewCreateTweetImage.setImageBitmap(imageBitmap)
                isTweetWithPhoto = true
            } else {
                setPicFromPath() // Get full quality image and scale to fit
                isTweetWithPhoto = true
            }
        } else if (requestCode == REQUEST_PICK_IMAGE && data?.data != null) {
            imageViewCreateTweetImage.visibility = View.VISIBLE
            imageViewCreateTweetImage.setImageURI(data.data)
            isTweetWithPhoto = true
        }
    }

    fun goBack(v: View) {
        finish()
    }

    fun takePicture(v: View) {
        dispatchTakePictureIntent()
    }

    fun openGallery(v: View) {
        openGallery()
    }

    private fun dispatchTakePictureIntent() {
        // This take only the thumbnail
        // val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // try {
        //    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        // } catch (e: ActivityNotFoundException) {
        //    displayToast(R.string.fail_to_open_camera)
        // }

        // Doc: https://developer.android.com/training/camera/photobasics#kotlin
        // The Android Camera application saves a full-size photo if you give it a file to save into.
        // You must provide a fully qualified file name where the camera app should save the photo.
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    displayToast(R.string.fail_to_open_camera)
                    null
                }
                // Continue only if the File was successfully created
                // AUTHORITY defined in AndroidManifest.xml
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "it.uninsubria.pdm.vellons.twitterclone.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyy-MM-dd-HHmmss").format(Date())
        // DIRECTORY_PICTURES refer to res/xml/file_paths.xml
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "Tweet_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    private fun setPicFromPath() {
        val bmOptions = BitmapFactory.Options().apply {
            inJustDecodeBounds = false
            inSampleSize = 4 // Scale factor
        }
        BitmapFactory.decodeFile(currentPhotoPath, bmOptions)?.also { bitmap ->
            imageViewCreateTweetImage.setImageBitmap(bitmap)
            imageViewCreateTweetImage.visibility = View.VISIBLE
        }
    }

    private fun openGallery() {
        Intent(Intent.ACTION_GET_CONTENT).also { intent ->
            intent.type = "image/*"
            intent.resolveActivity(packageManager)?.also {
                startActivityForResult(intent, REQUEST_PICK_IMAGE)
            }
        }
    }

    private fun displayToast(string: Int) {
        val toast = Toast.makeText(
            this@NewTweetActivity,
            getString(string),
            Toast.LENGTH_SHORT
        )
        toast.setGravity(Gravity.TOP, 0, 75)
        toast.show()
    }
}