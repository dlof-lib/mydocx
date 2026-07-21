package com.mydocx.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.mydocx.app.R

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val inputFullName = findViewById<TextInputEditText>(R.id.inputFullName)
        val inputUsername = findViewById<TextInputEditText>(R.id.inputUsername)
        val inputPassword = findViewById<TextInputEditText>(R.id.inputPassword)
        val inputConfirm = findViewById<TextInputEditText>(R.id.inputConfirmPassword)
        val btnRegister = findViewById<MaterialButton>(R.id.btnRegister)

        btnRegister.setOnClickListener {
            val fullName = inputFullName.text?.toString()?.trim().orEmpty()
            val username = inputUsername.text?.toString()?.trim()?.lowercase().orEmpty()
            val password = inputPassword.text?.toString().orEmpty()
            val confirm = inputConfirm.text?.toString().orEmpty()

            when {
                fullName.isEmpty() || username.isEmpty() -> toast("أكمل الاسم واسم المستخدم")
                !username.matches(Regex("^[a-z0-9_]{3,20}$")) ->
                    toast("اسم المستخدم يجب أن يكون بالإنجليزية/أرقام فقط (3-20 حرف)")
                password.length < 6 -> toast("كلمة السر يجب ألا تقل عن 6 أحرف")
                password != confirm -> toast("كلمتا السر غير متطابقتين")
                else -> {
                    val intent = Intent(this, PinSetupActivity::class.java).apply {
                        putExtra("full_name", fullName)
                        putExtra("username", username)
                        putExtra("password", password)
                    }
                    startActivity(intent)
                }
            }
        }

        findViewById<android.widget.TextView>(R.id.btnGoLogin).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
