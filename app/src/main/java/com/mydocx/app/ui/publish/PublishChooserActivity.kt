package com.mydocx.app.ui.publish

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mydocx.app.R

class PublishChooserActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_publish_chooser)

        findViewById<android.view.View>(R.id.cardArticle).setOnClickListener {
            startActivity(Intent(this, PublishArticleActivity::class.java))
        }
        findViewById<android.view.View>(R.id.cardProject).setOnClickListener {
            startActivity(Intent(this, PublishProjectActivity::class.java))
        }
    }
}
