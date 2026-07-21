package com.mydocx.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.mydocx.app.R
import com.mydocx.app.repo.AuthRepository
import com.mydocx.app.ui.main.MainActivity
import com.mydocx.app.util.SessionManager
import kotlinx.coroutines.launch

/** Login step 2: verify the red passkey PIN before entering the app. */
class PinEntryActivity : AppCompatActivity() {

    private lateinit var keypad: PinKeypadHelper
    private lateinit var authRepo: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin_entry)
        authRepo = AuthRepository(SessionManager(this))
        keypad = PinKeypadHelper(this) { pin -> checkPin(pin) }
    }

    private fun checkPin(pin: String) {
        lifecycleScope.launch {
            val error = findViewById<TextView>(R.id.tvPinError)
            when (val result = authRepo.verifyPin(pin)) {
                is AuthRepository.Result.Success -> {
                    startActivity(Intent(this@PinEntryActivity, MainActivity::class.java))
                    finish()
                }
                is AuthRepository.Result.Error -> {
                    error.text = result.message
                    error.visibility = TextView.VISIBLE
                    keypad.reset()
                }
            }
        }
    }
}
