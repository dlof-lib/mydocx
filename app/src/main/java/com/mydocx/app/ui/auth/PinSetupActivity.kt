package com.mydocx.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.mydocx.app.R
import com.mydocx.app.repo.AuthRepository
import com.mydocx.app.ui.main.MainActivity
import com.mydocx.app.util.SessionManager
import kotlinx.coroutines.launch

/** Step 2 of registration: user picks the 4-digit "red passkey" PIN, entered twice to confirm. */
class PinSetupActivity : AppCompatActivity() {

    private var firstPin: String? = null
    private lateinit var keypad: PinKeypadHelper
    private lateinit var authRepo: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin_entry)
        authRepo = AuthRepository(SessionManager(this))

        findViewById<TextView>(R.id.tvPinTitle).text = getString(R.string.red_passkey_title)
        findViewById<TextView>(R.id.tvPinDesc).text = getString(R.string.red_passkey_setup_desc)

        keypad = PinKeypadHelper(this) { pin -> onPinEntered(pin) }
    }

    private fun onPinEntered(pin: String) {
        val error = findViewById<TextView>(R.id.tvPinError)
        if (firstPin == null) {
            firstPin = pin
            error.visibility = TextView.INVISIBLE
            findViewById<TextView>(R.id.tvPinDesc).text = getString(R.string.confirm_pin)
            keypad.reset()
        } else {
            if (pin == firstPin) {
                createAccount(pin)
            } else {
                error.text = "الرمزان غير متطابقين، حاول مجددًا"
                error.visibility = TextView.VISIBLE
                firstPin = null
                findViewById<TextView>(R.id.tvPinDesc).text = getString(R.string.red_passkey_setup_desc)
                keypad.reset()
            }
        }
    }

    private fun createAccount(pin: String) {
        val fullName = intent.getStringExtra("full_name").orEmpty()
        val username = intent.getStringExtra("username").orEmpty()
        val password = intent.getStringExtra("password").orEmpty()

        lifecycleScope.launch {
            when (val result = authRepo.register(fullName, username, password, pin)) {
                is AuthRepository.Result.Success -> {
                    startActivity(Intent(this@PinSetupActivity, MainActivity::class.java))
                    finishAffinity()
                }
                is AuthRepository.Result.Error -> {
                    Toast.makeText(this@PinSetupActivity, result.message, Toast.LENGTH_SHORT).show()
                    firstPin = null
                    keypad.reset()
                }
            }
        }
    }
}
