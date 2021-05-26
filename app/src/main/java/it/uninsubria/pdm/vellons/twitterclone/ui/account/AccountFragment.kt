package it.uninsubria.pdm.vellons.twitterclone.ui.account

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import it.uninsubria.pdm.vellons.twitterclone.MainActivity
import it.uninsubria.pdm.vellons.twitterclone.R
import java.io.ByteArrayOutputStream
import java.util.*
import java.util.regex.Pattern


class AccountFragment : Fragment() {
    private val TAG = "AccountFragment"
    private val REQUEST_PICK_IMAGE_FOR_ACCOUNT = 1003
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private var isUserVerified: Boolean = false

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        auth = Firebase.auth
        firestore = Firebase.firestore
        storage = Firebase.storage
        val root = inflater.inflate(R.layout.fragment_account, container, false)
        val textViewTitle: TextView = root.findViewById(R.id.textViewTitle)
        val textViewProfileName: TextView = root.findViewById(R.id.textViewProfileName)
        val textViewProfileUsername: TextView = root.findViewById(R.id.textViewProfileUsername)
        val textViewProfileBio: TextView = root.findViewById(R.id.textViewProfileBio)
        val imageViewBadge: ImageView = root.findViewById(R.id.imageViewProfileVerifiedBadgeEdit)
        val editTextProfileName: TextView = root.findViewById(R.id.editTextProfileName)
        val editTextProfileUsername: TextView = root.findViewById(R.id.editTextProfileUsername)
        val editTextProfileBio: TextView = root.findViewById(R.id.editTextProfileBio)
        val buttonEditAccount: Button = root.findViewById(R.id.buttonEditAccount)
        val buttonEditPhoto: Button = root.findViewById(R.id.buttonEditPhoto)
        val buttonLogout: Button = root.findViewById(R.id.buttonLogout)
        val imageViewProfileUserImage: ShapeableImageView =
            root.findViewById(R.id.imageViewProfileUserImage)
        imageViewBadge.visibility = View.GONE
        textViewProfileName.text = ""
        textViewProfileUsername.text = ""
        textViewProfileBio.text = ""

        // Get FirebaseAuth user instance
        val currentUser = auth.currentUser
        val uid = currentUser?.uid
        val firestoreSource = Source.SERVER // Avoid using Firestore cache
        if (currentUser == null || uid == null) {
            Log.d(TAG, "User not logged")
            // Redirect MainPage
            val intent = Intent(context, MainActivity::class.java)
            startActivity(intent)
            activity?.finish()
        } else {
            // Get current users info from DB
            val userRef = firestore.collection("users").document(uid)
            userRef.get(firestoreSource).addOnSuccessListener { document ->
                Log.d(TAG, "Current user uid: $uid Data: " + document.data)
                if (document != null) {
                    textViewProfileName.text = document.getString("name")
                    textViewProfileUsername.text = "@" + document.getString("username")
                    textViewProfileBio.text = document.getString("bio")

                    isUserVerified = document.getBoolean("verified") == true
                    if (isUserVerified) {
                        imageViewBadge.visibility = View.VISIBLE // Only if verified user
                    }

                    val profilePath = document.getString("photo")
                    if (profilePath != null) { // Download profile photo if present
                        buttonEditPhoto.text = getString(R.string.edit_photo)
                        val storageRef = storage.reference
                        val profilePhotoRef = storageRef.child(profilePath)

                        val ONE_MEGABYTE: Long = 1024 * 1024
                        // .getBytes take as a parameter the maximum download size accepted
                        profilePhotoRef.getBytes(8 * ONE_MEGABYTE)
                            .addOnSuccessListener { bytes ->
                                val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                imageViewProfileUserImage.setImageBitmap(bmp)
                            }.addOnFailureListener {
                                Log.e(TAG, "Failed to download user image ", it)
                            }
                    }
                } else {
                    Log.d(TAG, "No such document. User do not exists in DB")
                }
            }.addOnFailureListener { exception ->
                textViewProfileName.text = ""
                textViewProfileUsername.text = ""
                textViewProfileBio.text = getString(R.string.check_internet_connection)
                Log.e(TAG, "Failed to get user info ", exception)
                displayToast(R.string.check_internet_connection)
            }
        }

        buttonEditAccount.setOnClickListener { // Edit account button click
            if (textViewProfileUsername.text == "") {
                return@setOnClickListener
            }
            if (textViewTitle.text == getString(R.string.your_account)) {
                // Enable edit mode
                textViewTitle.text = getString(R.string.edit_your_account)
                imageViewBadge.visibility = View.GONE
                textViewProfileName.visibility = View.GONE
                textViewProfileUsername.visibility = View.GONE
                textViewProfileBio.visibility = View.GONE
                editTextProfileName.text = textViewProfileName.text
                editTextProfileUsername.text = textViewProfileUsername.text.toString()
                    .replace("@", "").toLowerCase(Locale.ROOT)
                editTextProfileBio.text = textViewProfileBio.text
                editTextProfileName.visibility = View.VISIBLE
                editTextProfileUsername.visibility = View.VISIBLE
                editTextProfileBio.visibility = View.VISIBLE
                buttonEditAccount.text = getString(R.string.save_account)
            } else {
                // Disable edit mode (check if i need to save data)
                val name: String = editTextProfileName.text.toString()
                var user: String = editTextProfileUsername.text.toString()
                val bio: String = editTextProfileBio.text.toString()
                var errors = 0

                if (!isValidName(name)) {
                    errors += 1
                    editTextProfileName.error = getString(R.string.invalid_name)
                }
                if (!isValidUsername(user)) {
                    errors += 1
                    editTextProfileUsername.error = getString(R.string.invalid_username)
                }

                if (errors == 0) { // Check if errors are displayed in EditText
                    user = user.toLowerCase(Locale.ROOT)
                    // Check if user update profile info
                    if (name != textViewProfileName.text || ("@$user") != textViewProfileUsername.text || textViewProfileBio.text != bio) {
                        // Data changed!
                        // Check if username is not used by others
                        firestore.collection("users")
                            .whereEqualTo("username", user)
                            .get(firestoreSource)
                            .addOnSuccessListener { result ->
                                for (document in result) {
                                    Log.d(TAG, "Username: ${document.id} => ${document.data}")
                                    if (document.getString("username") == user && document.id != uid) { // Username already used
                                        editTextProfileUsername.error =
                                            getString(R.string.invalid_username_already_used)
                                        return@addOnSuccessListener
                                    }
                                }

                                // Update new user info to Firebase
                                if (currentUser != null && uid != null) {
                                    // Get current users info from DB
                                    val userRef = firestore.collection("users").document(uid)
                                    // Update user info
                                    userRef.update("name", name)
                                    userRef.update("username", user)
                                    userRef.update("bio", bio)
                                    userRef.update("updatedAt", FieldValue.serverTimestamp())
                                        .addOnSuccessListener {
                                            displayToast(R.string.edit_account_completed)
                                            Log.d(TAG, "User successfully updated!")

                                            // Show new data in text view
                                            textViewProfileName.text = name
                                            textViewProfileUsername.text = ("@$user")
                                            textViewProfileBio.text = bio

                                            // Restore normal mode
                                            textViewTitle.text = getString(R.string.your_account)
                                            editTextProfileName.visibility = View.GONE
                                            editTextProfileUsername.visibility = View.GONE
                                            editTextProfileBio.visibility = View.GONE
                                            textViewProfileName.visibility = View.VISIBLE
                                            textViewProfileUsername.visibility = View.VISIBLE
                                            textViewProfileBio.visibility = View.VISIBLE
                                            buttonEditAccount.text =
                                                getString(R.string.edit_account)
                                            if (isUserVerified) {
                                                imageViewBadge.visibility =
                                                    View.VISIBLE // Only if verified user
                                            }
                                        }
                                        .addOnFailureListener { exception ->
                                            displayToast(R.string.check_internet_connection)
                                            Log.e(TAG, "Error updating user to DB", exception)
                                        }
                                }
                            }
                            .addOnFailureListener { exception ->
                                displayToast(R.string.check_internet_connection)
                                Log.e(TAG, "Error checking username", exception)
                            }

                    } else {
                        // Restore normal mode if data not changed
                        textViewTitle.text = getString(R.string.your_account)
                        editTextProfileName.visibility = View.GONE
                        editTextProfileUsername.visibility = View.GONE
                        editTextProfileBio.visibility = View.GONE
                        textViewProfileName.visibility = View.VISIBLE
                        textViewProfileUsername.visibility = View.VISIBLE
                        textViewProfileBio.visibility = View.VISIBLE
                        buttonEditAccount.text = getString(R.string.edit_account)
                        if (isUserVerified) {
                            imageViewBadge.visibility = View.VISIBLE // Only if verified user
                        }
                    }
                } // end errors == 0
            }
        } // end buttonEditAccount.setOnClickListener

        buttonEditPhoto.setOnClickListener {
            openGallery()
        }

        buttonLogout.setOnClickListener {
            auth.signOut()
            Log.d(TAG, "Logout completed")
            // Redirect MainPage
            val intent = Intent(context, MainActivity::class.java)
            startActivity(intent)
            activity?.finish()
        }

        buttonLogout.setOnLongClickListener {
            displayToast(R.string.app_info_message)
            return@setOnLongClickListener true
        }

        return root
    } // end onCreateView()

    private fun isValidName(str: String?): Boolean {
        return str != null && str.length >= 4
    }

    private fun isValidUsername(str: String?): Boolean {
        if (str == null) return false
        val lowerUsername = str.toLowerCase(Locale.ROOT)
        val usernamePattern = ("^[a-z0-9_-]+\$")
        val pattern = Pattern.compile(usernamePattern)
        val matcher = pattern.matcher(lowerUsername)
        return str.length >= 4 && matcher.matches()
    }

    private fun displayToast(string: Int) {
        val toast = Toast.makeText(
            context,
            context?.getString(string),
            Toast.LENGTH_SHORT
        )
        toast.setGravity(Gravity.TOP, 0, 75)
        toast.show()
    }

    private fun openGallery() {
        Intent(Intent.ACTION_GET_CONTENT).also { intent ->
            intent.type = "image/*"
            activity?.let {
                intent.resolveActivity(it.packageManager)?.also {
                    startActivityForResult(intent, REQUEST_PICK_IMAGE_FOR_ACCOUNT)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // Upload image to Firebase
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_PICK_IMAGE_FOR_ACCOUNT && data?.data != null) { // Save selected image to Firebase storage
            val uri = data.data
            val imageViewProfileUserImage: ShapeableImageView =
                requireActivity().findViewById(R.id.imageViewProfileUserImage)
            imageViewProfileUserImage.setImageURI(uri) // Set photo on account image

            val buttonEditPhoto: Button = requireActivity().findViewById(R.id.buttonEditPhoto)
            buttonEditPhoto.text = getString(R.string.edit_photo)

            // Get user id to save image on storage
            val uid = auth.currentUser!!.uid

            // Create reference for the image to upload
            val storageRef = storage.reference
            val profilePhotoRef = storageRef.child("users-photo/${uid}.png")

            // Prepare image stream
            imageViewProfileUserImage.isDrawingCacheEnabled = true
            imageViewProfileUserImage.buildDrawingCache()
            val bitmap = (imageViewProfileUserImage.drawable as BitmapDrawable).bitmap
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
            val toUploadData = baos.toByteArray()

            val uploadTask = profilePhotoRef.putBytes(toUploadData) // Upload task
            uploadTask.addOnFailureListener {
                Log.e(TAG, "Storage upload failed for " + profilePhotoRef.path)
                displayToast(R.string.upload_image_failed)
            }.addOnSuccessListener { taskSnapshot ->
                Log.d(
                    TAG, "Storage upload confirmed for " + profilePhotoRef.path
                            + " size: " + taskSnapshot.metadata?.sizeBytes
                )
                displayToast(R.string.upload_image_succes)

                // Save path to Firestore user profile
                val userRef = firestore.collection("users").document(uid)
                // Update user infos
                userRef.update("photo", profilePhotoRef.path)
                userRef.update("updatedAt", FieldValue.serverTimestamp())
                    .addOnSuccessListener {
                        displayToast(R.string.edit_account_completed)
                        Log.d(TAG, "User photo successfully updated!")
                    }
                    .addOnFailureListener { exception ->
                        displayToast(R.string.check_internet_connection)
                        Log.e(TAG, "Error updating user photo to DB", exception)
                    }
            }
        }
    }
}