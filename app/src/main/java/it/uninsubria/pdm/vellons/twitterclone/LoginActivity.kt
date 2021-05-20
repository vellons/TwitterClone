package it.uninsubria.pdm.vellons.twitterclone

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.*
import java.util.regex.Pattern

class LoginActivity : AppCompatActivity() {
    private val TAG = "LoginActivity"
    private lateinit var auth: FirebaseAuth
    var email: String = ""
    var pass: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_account)
        auth = Firebase.auth
    }

    fun goBack(v: View) {
        finish()
    }

    fun validateAndLogin(v: View) {
        val editTextEmail = findViewById<EditText>(R.id.editTextLoginEmail)
        val editTextPassword1 = findViewById<EditText>(R.id.editTextLoginPassword1)

        email = editTextEmail.text.toString()
        pass = editTextPassword1.text.toString()
        var errors = 0

        if (!isValidEmail(email)) {
            errors += 1
            editTextEmail.error = getString(R.string.invalid_email)
        }
        if (!isValidPassword(pass)) {
            errors += 1
            editTextPassword1.error = getString(R.string.invalid_password)
        }
        if (errors == 0) {
            login(email, pass)
        }
    }

    fun resetPassword(v: View) {
        val editTextEmail = findViewById<EditText>(R.id.editTextLoginEmail)
        email = editTextEmail.text.toString()

        if (isValidEmail(email)) {
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "sendPasswordResetEmail: success")
                        displayToast(getString(R.string.reset_password_instruction))
                    } else {
                        Log.w(TAG, "sendPasswordResetEmail: failure", task.exception)
                        displayToast(getString(R.string.email_or_password_wrong))
                    }
                }
        } else {
            editTextEmail.error = getString(R.string.invalid_email)
        }
    }

    private fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithEmail: success")
                    val user = auth.currentUser
                    val uid = user?.uid
                    Log.d(TAG, "Logged user:" + uid.toString())
                    // Redirect to navigator if user is logged
                    val intent = Intent(this@LoginActivity, NavigatorActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Log.w(TAG, "signInWithEmail: failure", task.exception)
                    displayToast(getString(R.string.email_or_password_wrong))
                }
            }
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