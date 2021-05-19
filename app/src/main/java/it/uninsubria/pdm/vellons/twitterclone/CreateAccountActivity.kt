package it.uninsubria.pdm.vellons.twitterclone

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*
import java.util.regex.Pattern

class CreateAccountActivity : AppCompatActivity() {
    private val TAG = "CreateAccountActivity"
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    var name: String = ""
    var user: String = ""
    var email: String = ""
    var pass: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)
        auth = Firebase.auth
        firestore = Firebase.firestore
    }

    fun goBack(v: View) {
        finish()
    }

    fun validateInputsAndContinue(v: View) {
        val editTextName = findViewById<EditText>(R.id.editTextName)
        val editTextUsername = findViewById<EditText>(R.id.editTextUsername)
        val editTextEmail = findViewById<EditText>(R.id.editTextEmail)
        val editTextPassword1 = findViewById<EditText>(R.id.editTextPassword1)

        name = editTextName.text.toString()
        user = editTextUsername.text.toString()
        email = editTextEmail.text.toString()
        pass = editTextPassword1.text.toString()
        var errors = 0

        if (!isValidName(name)) {
            errors += 1
            editTextName.error = getString(R.string.invalid_name)
        }
        if (!isValidEmail(email)) {
            errors += 1
            editTextEmail.error = getString(R.string.invalid_email)
        }
        if (!isValidPassword(pass)) {
            errors += 1
            editTextPassword1.error = getString(R.string.invalid_password)
        }
        if (!isValidUsername(user)) {
            errors += 1
            editTextUsername.error = getString(R.string.invalid_username)
        }
        if (errors == 0) {
            checkUsernameAndCreateAccount(user)
        }
    }

    private fun isValidName(str: String?): Boolean {
        return str != null && str.length >= 4
    }

    private fun isValidEmail(email: String?): Boolean {
        if (email == null || email.isEmpty()) return false
        val emailPattern =
            ("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$")
        val pattern = Pattern.compile(emailPattern)
        val matcher = pattern.matcher(email)
        return matcher.matches()
    }

    private fun isValidPassword(str: String?): Boolean {
        return str != null && str.length >= 8
    }

    private fun isValidUsername(str: String?): Boolean {
        if (str == null) return false
        val lowerUsername = str.toLowerCase(Locale.ROOT)
        val usernamePattern = ("^[a-z0-9_-]+\$")
        val pattern = Pattern.compile(usernamePattern)
        val matcher = pattern.matcher(lowerUsername)
        return str.length >= 4 && matcher.matches()
    }


    private fun checkUsernameAndCreateAccount(str: String?) {
        if (str == null) return
        val lowerUsername = str.toLowerCase(Locale.ROOT)
        // Check if username already exists in database
        firestore.collection("users")
            .whereEqualTo("username", lowerUsername)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    Log.d(TAG, "${document.id} => ${document.data}")
                    if (document["username"] == lowerUsername) { // Username already exist
                        val editTextUsername = findViewById<EditText>(R.id.editTextUsername)
                        editTextUsername.error = getString(R.string.invalid_username_already_used)
                        return@addOnSuccessListener
                    }
                }
                createAccount(email, pass)
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents. Failed to check username", exception)
                displayToast("Failed to check username")
            }
    }

    private fun createAccount(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail: success")
                    val user = auth.currentUser
                    val uid = user?.uid
                    if (uid != null) {
                        Log.d(TAG, "Registered user: $uid. Saving in users collection")
                        saveUser(uid)
                    } else {
                        Log.w(TAG, "Failed to get uid!")
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail: failure", task.exception)
                    displayToast(
                        "Authentication failed: " + (task.exception?.message ?: " check logs")
                    )
                }
            }
    }

    private fun saveUser(uid: String) {
        val user = hashMapOf(
            "username" to user,
            "verified" to false,
            "name" to name,
            "photo" to null,
            "registeredAt" to FieldValue.serverTimestamp(),
            "bio" to ""
        )
        firestore.collection("users").document(uid)
            .set(user)
            .addOnSuccessListener {
                Log.d(TAG, "User $uid saved to DB")
                displayToast("YOU ARE IN!")
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error writing document. Failed to save user in collection", exception)
                displayToast("Failed to save user to DB")
            }
    }


    private fun displayToast(string: String) {
        val toast = Toast.makeText(
            this,
            string,
            Toast.LENGTH_SHORT
        )
        toast.setGravity(Gravity.TOP, 0, 75)
        toast.show()
    }
}