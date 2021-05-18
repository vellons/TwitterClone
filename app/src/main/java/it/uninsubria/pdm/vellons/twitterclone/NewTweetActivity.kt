package it.uninsubria.pdm.vellons.twitterclone

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class NewTweetActivity : AppCompatActivity() {
    private val REQUEST_IMAGE_CAPTURE = 1001
    private val REQUEST_PICK_IMAGE = 1002

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_tweet)

        val editTextNewTweetText: EditText = findViewById(R.id.editTextNewTweetText)
        editTextNewTweetText.requestFocus()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
//            val imageBitmap = data.extras.get("data") as Bitmap
//            imageView.setImageBitmap(imageBitmap)
        } else if (requestCode == REQUEST_PICK_IMAGE) {
//            val uri = data?.getData()
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

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } catch (e: ActivityNotFoundException) {
            val toast = Toast.makeText(
                applicationContext,
                getString(R.string.fail_to_open_camera),
                Toast.LENGTH_SHORT
            )
            toast.setGravity(Gravity.TOP, 0, 75)
            toast.show()
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
}