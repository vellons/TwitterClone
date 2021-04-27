package it.uninsubria.pdm.vellons.twitterclone

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun goToCreateAccount(v: View) {
        val intent = Intent(this@MainActivity, CreateAccountActivity::class.java)
        startActivity(intent)
    }

    fun goToLogin(v: View) {
        val intent = Intent(this@MainActivity, NavigatorActivity::class.java)
        startActivity(intent)
    }

    fun showInfo(v: View) {
        val toast = Toast.makeText(
            applicationContext,
            getString(R.string.app_info_message),
            Toast.LENGTH_SHORT
        )
        toast.setGravity(Gravity.TOP, 0, 75)
        toast.show()
    }
}