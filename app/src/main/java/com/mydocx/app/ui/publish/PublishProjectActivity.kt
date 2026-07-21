package com.mydocx.app.ui.publish

import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.mydocx.app.R
import com.mydocx.app.network.StorageUploader
import com.mydocx.app.repo.ContentRepository
import com.mydocx.app.util.SessionManager
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

/**
 * Publishes a code project. "Attach files" lets the user pick a single .zip
 * that preserves their project's folder structure — this is what powers the
 * "تحميل هيكلي" (structured download) button on the feed/project card later:
 * other users download the exact same zip, structure intact.
 */
class PublishProjectActivity : AppCompatActivity() {

    private var pickedZipUri: Uri? = null
    private lateinit var session: SessionManager
    private lateinit var repo: ContentRepository

    private val pickZip = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            pickedZipUri = uri
            findViewById<TextView>(R.id.tvAttachedFile).text = "تم اختيار الملف: ${uri.lastPathSegment}"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_publish_project)
        setSupportActionBar(findViewById<Toolbar>(R.id.toolbar))
        findViewById<Toolbar>(R.id.toolbar).setNavigationOnClickListener { finish() }

        session = SessionManager(this)
        repo = ContentRepository(session)

        findViewById<MaterialButton>(R.id.btnAttachFiles).setOnClickListener {
            pickZip.launch("application/zip")
        }

        findViewById<MaterialButton>(R.id.btnPublish).setOnClickListener { publish() }
    }

    private fun publish() {
        val name = findViewById<EditText>(R.id.inputName).text.toString().trim()
        val description = findViewById<EditText>(R.id.inputDescription).text.toString().trim()
        val language = findViewById<EditText>(R.id.inputLanguage).text.toString().trim().ifBlank { null }
        val repoUrl = findViewById<EditText>(R.id.inputRepoUrl).text.toString().trim().ifBlank { null }

        if (name.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "أكمل اسم المشروع والوصف", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            var storageUrl: String? = null
            val zipUri = pickedZipUri
            val myId = session.userId
            if (zipUri != null && myId != null) {
                val tmp = File(cacheDir, "project_upload.zip")
                contentResolver.openInputStream(zipUri)?.use { input ->
                    FileOutputStream(tmp).use { output -> input.copyTo(output) }
                }
                storageUrl = StorageUploader.upload(
                    bucket = "projects",
                    path = "$myId/${System.currentTimeMillis()}_${name.replace(" ", "_")}.zip",
                    file = tmp,
                    accessToken = session.accessToken ?: "",
                    mime = "application/zip"
                )
            }

            val result = repo.publishProject(name, description, repoUrl, storageUrl, language)
            if (result != null) {
                Toast.makeText(this@PublishProjectActivity, "تم نشر المشروع", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this@PublishProjectActivity, "تعذر النشر، حاول مجددًا", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
