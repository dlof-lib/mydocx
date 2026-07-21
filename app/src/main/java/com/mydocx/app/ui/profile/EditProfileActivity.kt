package com.mydocx.app.ui.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.load
import coil.transform.CircleCropTransformation
import com.mydocx.app.R
import com.mydocx.app.model.SocialLink
import com.mydocx.app.network.StorageUploader
import com.mydocx.app.repo.ContentRepository
import com.mydocx.app.util.SessionManager
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class EditProfileActivity : AppCompatActivity() {

    private lateinit var session: SessionManager
    private lateinit var repo: ContentRepository
    private var pickedAvatarUri: Uri? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            pickedAvatarUri = uri
            findViewById<ImageView>(R.id.imgAvatarPreview).load(uri) { transformations(CircleCropTransformation()) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        session = SessionManager(this)
        repo = ContentRepository(session)
        val myId = session.userId ?: return finish()

        lifecycleScope.launch {
            val profile = repo.getProfile(myId) ?: return@launch
            findViewById<EditText>(R.id.inputFullName).setText(profile.fullName)
            findViewById<EditText>(R.id.inputBio).setText(profile.bio)
            findViewById<EditText>(R.id.inputSpecialThanks).setText(profile.specialThanks)
            profile.socialLinks?.forEach { link ->
                when (link.label.lowercase()) {
                    "github" -> findViewById<EditText>(R.id.inputGithub).setText(link.url)
                    "x", "twitter" -> findViewById<EditText>(R.id.inputTwitter).setText(link.url)
                    "website" -> findViewById<EditText>(R.id.inputWebsite).setText(link.url)
                }
            }
            if (!profile.avatarUrl.isNullOrEmpty()) {
                findViewById<ImageView>(R.id.imgAvatarPreview).load(profile.avatarUrl) {
                    transformations(CircleCropTransformation())
                }
            }
        }

        findViewById<android.view.View>(R.id.btnUploadAvatar).setOnClickListener {
            pickImage.launch("image/*")
        }

        findViewById<android.view.View>(R.id.btnSave).setOnClickListener { save(myId) }
    }

    private fun save(myId: String) {
        val fullName = findViewById<EditText>(R.id.inputFullName).text.toString().trim()
        val bio = findViewById<EditText>(R.id.inputBio).text.toString().trim()
        val thanks = findViewById<EditText>(R.id.inputSpecialThanks).text.toString().trim()
        val github = findViewById<EditText>(R.id.inputGithub).text.toString().trim()
        val twitter = findViewById<EditText>(R.id.inputTwitter).text.toString().trim()
        val website = findViewById<EditText>(R.id.inputWebsite).text.toString().trim()

        val links = mutableListOf<SocialLink>()
        if (github.isNotEmpty()) links.add(SocialLink("GitHub", github))
        if (twitter.isNotEmpty()) links.add(SocialLink("X", twitter))
        if (website.isNotEmpty()) links.add(SocialLink("Website", website))

        lifecycleScope.launch {
            var avatarUrl: String? = null
            val uri = pickedAvatarUri
            if (uri != null) {
                val tmp = File(cacheDir, "avatar_upload.jpg")
                contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(tmp).use { output -> input.copyTo(output) }
                }
                avatarUrl = StorageUploader.upload(
                    bucket = "avatars",
                    path = "$myId.jpg",
                    file = tmp,
                    accessToken = session.accessToken ?: "",
                    mime = "image/jpeg"
                )
            }

            val fields = mutableMapOf<String, Any?>(
                "full_name" to fullName,
                "bio" to bio,
                "special_thanks" to thanks,
                "social_links" to links
            )
            if (avatarUrl != null) fields["avatar_url"] = avatarUrl

            repo.updateProfile(myId, fields)
            Toast.makeText(this@EditProfileActivity, "تم الحفظ", Toast.LENGTH_SHORT).show()
            setResult(RESULT_OK)
            finish()
        }
    }
}
