package com.mydocx.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.mydocx.app.R
import com.mydocx.app.repo.AuthRepository
import com.mydocx.app.ui.main.MainActivity
import com.mydocx.app.util.SessionManager
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var session: SessionManager
    private lateinit var authRepo: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        session = SessionManager(this)
        authRepo = AuthRepository(session)

        val inputUsername = findViewById<TextInputEditText>(R.id.inputUsername)
        val inputPassword = findViewById<TextInputEditText>(R.id.inputPassword)
        val btnLogin = findViewById<MaterialButton>(R.id.btnLogin)

        btnLogin.setOnClickListener {
            val username = inputUsername.text?.toString()?.trim().orEmpty()
            val password = inputPassword.text?.toString().orEmpty()
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "أدخل اسم المستخدم وكلمة السر", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            btnLogin.isEnabled = false
            lifecycleScope.launch {
                when (val result = authRepo.login(username, password)) {
                    is AuthRepository.Result.Success -> {
                        startActivity(Intent(this@LoginActivity, PinEntryActivity::class.java))
                        finish()
                    }
                    is AuthRepository.Result.Error -> {
                        btnLogin.isEnabled = true
                        Toast.makeText(this@LoginActivity, result.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        findViewById<android.widget.TextView>(R.id.btnGoRegister).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}
