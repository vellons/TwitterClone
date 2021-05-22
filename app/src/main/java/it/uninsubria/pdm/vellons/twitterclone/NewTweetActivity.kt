package it.uninsubria.pdm.vellons.twitterclone

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage

class NewTweetActivity : AppCompatActivity() {
    private val TAG = "NewTweetActivity"
    private val REQUEST_IMAGE_CAPTURE = 1001
    private val REQUEST_PICK_IMAGE = 1002
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_tweet)
        auth = Firebase.auth
        firestore = Firebase.firestore
        storage = Firebase.storage

        val imageViewProfileUserImage: ShapeableImageView =
            findViewById(R.id.imageViewProfileUserImage)
        val editTextNewTweetText: EditText = findViewById(R.id.editTextNewTweetText)
        val buttonPostTweet: Button = findViewById(R.id.buttonPostTweet)

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
//            val imageBitmap = data.extras.get("data") as Bitmap
//            imageView.setImageBitmap(imageBitmap)
        } else if (requestCode == REQUEST_PICK_IMAGE && data?.data != null) {
//            val uri = data.data
//            imageView.setImageURI(uri)
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

    fun setTweetSource(v: View) {
        displayToast(R.string.not_implemented_yet)
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } catch (e: ActivityNotFoundException) {
            displayToast(R.string.fail_to_open_camera)
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