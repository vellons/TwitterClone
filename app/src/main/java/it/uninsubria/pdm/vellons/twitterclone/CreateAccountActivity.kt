package it.uninsubria.pdm.vellons.twitterclone

import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import java.util.*
import java.util.regex.Pattern

class CreateAccountActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)
    }

    fun goBack(v: View) {
        finish()
    }

    fun validateInputsAndContinue(v: View) {
        val editTextUsername = findViewById<EditText>(R.id.editTextUsername)
        val editTextEmail = findViewById<EditText>(R.id.editTextEmail)
        val editTextPassword1 = findViewById<EditText>(R.id.editTextPassword1)

        val user: String = editTextUsername.text.toString()
        val email: String = editTextEmail.text.toString()
        val pass: String = editTextPassword1.text.toString()

        if (!isValidEmail(email)) {
            editTextEmail.error = getString(R.string.invalid_email)
        }
        if (!isValidPassword(pass)) {
            editTextPassword1.error = getString(R.string.invalid_password)
        }
        if (!isValidUsername(user)) {
            editTextUsername.error = getString(R.string.invalid_username)
        }
        if (isValidUsername(user) && !isFreeUsername(user)) {
            editTextUsername.error = getString(R.string.invalid_username_already_used)
        }
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
}