package it.uninsubria.pdm.vellons.twitterclone

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class NewTweetActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_tweet)
    }

    fun goBack(v: View) {
        finish()
    }
}