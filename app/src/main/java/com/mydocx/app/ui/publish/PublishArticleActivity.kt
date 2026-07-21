package com.mydocx.app.ui.publish

import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.mydocx.app.R
import com.mydocx.app.repo.ContentRepository
import com.mydocx.app.util.SessionManager
import kotlinx.coroutines.launch

class PublishArticleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_publish_article)
        setSupportActionBar(findViewById<Toolbar>(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        findViewById<Toolbar>(R.id.toolbar).setNavigationOnClickListener { finish() }

        val session = SessionManager(this)
        val repo = ContentRepository(session)

        findViewById<MaterialButton>(R.id.btnPublish).setOnClickListener {
            val title = findViewById<EditText>(R.id.inputTitle).text.toString().trim()
            val body = findViewById<EditText>(R.id.inputBody).text.toString().trim()
            if (title.isEmpty() || body.isEmpty()) {
                Toast.makeText(this, "أكمل العنوان والمحتوى", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            lifecycleScope.launch {
                val result = repo.publishArticle(title, body, coverUrl = null)
                if (result != null) {
                    Toast.makeText(this@PublishArticleActivity, "تم النشر", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@PublishArticleActivity, "تعذر النشر، حاول مجددًا", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
