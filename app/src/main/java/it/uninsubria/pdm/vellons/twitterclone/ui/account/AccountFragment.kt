package it.uninsubria.pdm.vellons.twitterclone.ui.account

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import it.uninsubria.pdm.vellons.twitterclone.MainActivity
import it.uninsubria.pdm.vellons.twitterclone.R
import java.util.*
import java.util.regex.Pattern

class AccountFragment : Fragment() {
    private val TAG = "AccountFragment"
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        auth = Firebase.auth
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

        buttonEditAccount.setOnClickListener {
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
                // Disable edit mode
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
                if (isValidUsername(user) && !isFreeUsername(user)) {
                    errors += 1
                    editTextProfileUsername.error =
                        getString(R.string.invalid_username_already_used)
                }
                if (errors == 0) { // Check if errors are displayed in EditText
                    user = user.toLowerCase(Locale.ROOT)
                    if (name != textViewProfileName.text || ("@$user") != textViewProfileUsername.text || textViewProfileBio.text != bio) {
                        // Check if user update profile info
                        displayToast(R.string.edit_account_completed)
                        textViewProfileName.text = name
                        textViewProfileUsername.text = ("@$user")
                        textViewProfileBio.text = bio
                        // Call Firebase here!
                    }
                    // Restore normal mode
                    textViewTitle.text = getString(R.string.your_account)
                    editTextProfileName.visibility = View.GONE
                    editTextProfileUsername.visibility = View.GONE
                    editTextProfileBio.visibility = View.GONE
                    imageViewBadge.visibility = View.VISIBLE // Only if verified user
                    textViewProfileName.visibility = View.VISIBLE
                    textViewProfileUsername.visibility = View.VISIBLE
                    textViewProfileBio.visibility = View.VISIBLE
                    buttonEditAccount.text = getString(R.string.edit_account)
                }
            }
        }

        buttonEditPhoto.setOnClickListener {
            displayToast(R.string.not_implemented_yet)
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
    }

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

    private fun isFreeUsername(str: String?): Boolean {
        if (str == null) return false
        val lowerUsername = str.toLowerCase(Locale.ROOT)
        return lowerUsername != "alex"
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
}