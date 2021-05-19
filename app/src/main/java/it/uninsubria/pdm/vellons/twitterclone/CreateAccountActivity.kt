package it.uninsubria.pdm.vellons.twitterclone

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.*
import java.util.regex.Pattern

class CreateAccountActivity : AppCompatActivity() {
    private val TAG = "CreateAccountActivity"
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)
        auth = Firebase.auth
    }

    fun goBack(v: View) {
        finish()
    }

    fun validateInputsAndContinue(v: View) {
        val editTextName = findViewById<EditText>(R.id.editTextName)
        val editTextUsername = findViewById<EditText>(R.id.editTextUsername)
        val editTextEmail = findViewById<EditText>(R.id.editTextEmail)
        val editTextPassword1 = findViewById<EditText>(R.id.editTextPassword1)

        val name: String = editTextName.text.toString()
        val user: String = editTextUsername.text.toString()
        val email: String = editTextEmail.text.toString()
        val pass: String = editTextPassword1.text.toString()
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
        if (isValidUsername(user) && !isFreeUsername(user)) {
            errors += 1
            editTextUsername.error = getString(R.string.invalid_username_already_used)
        }
        if (errors == 0) {
            createAccount(email, pass)
        }
    }

    private fun isValidName(str: String?): Boolean {
        return str != null && str.length >= 4
    }

    private fun isValidEmail(email: String?): Boolean {
        if (email == null) return false
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

    private fun isFreeUsername(str: String?): Boolean {
        if (str == null) return false
        val lowerUsername = str.toLowerCase(Locale.ROOT)
        return lowerUsername != "alex"
    }

    private fun createAccount(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail: success")
                    val user = auth.currentUser
                    val uid = user?.uid
                    Log.d(TAG, "Registered user:" + uid.toString())
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail: failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Authentication failed: " + (task.exception?.message ?: "check logs"),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}