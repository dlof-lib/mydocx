package com.mydocx.app.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.mydocx.app.ui.auth.LoginActivity
import com.mydocx.app.ui.auth.PinEntryActivity
import com.mydocx.app.ui.main.MainActivity
import com.mydocx.app.util.SessionManager

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val session = SessionManager(this)

        Handler(Looper.getMainLooper()).postDelayed({
            val next = when {
                !session.isLoggedIn -> Intent(this, LoginActivity::class.java)
                !session.pinVerified -> Intent(this, PinEntryActivity::class.java)
                else -> Intent(this, MainActivity::class.java)
            }
            startActivity(next)
            finish()
        }, 700)
    }
}
