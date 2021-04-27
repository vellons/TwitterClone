package it.uninsubria.pdm.vellons.twitterclone

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class UserDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_detail)
        val profileId = intent.getStringExtra("id")
        val profileName = intent.getStringExtra("name")
        val profileUsername = intent.getStringExtra("username")
        val profileUserVerified = intent.getBooleanExtra("userVerified", false)

        if (profileId != null && profileName != null && profileUsername != null) {
            val name: TextView = findViewById(R.id.textViewProfileName)
            val username: TextView = findViewById(R.id.textViewProfileUsername)
            val verifiedBadge: ImageView = findViewById(R.id.imageViewProfileVerifiedBadge)
            val bio: TextView = findViewById(R.id.textViewProfileBio)

            name.text = profileName
            username.text =  ("@$profileUsername")
            verifiedBadge.visibility = if (profileUserVerified) View.VISIBLE else View.GONE
            bio.text = ("$profileName. Ciao!!")
        }
    }

    fun goBack(v: View) {
        finish()
    }
}