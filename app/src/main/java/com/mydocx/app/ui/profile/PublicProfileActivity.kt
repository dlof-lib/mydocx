package com.mydocx.app.ui.profile

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import coil.load
import coil.transform.CircleCropTransformation
import com.google.android.material.button.MaterialButton
import com.mydocx.app.R
import com.mydocx.app.repo.ContentRepository
import com.mydocx.app.util.AvatarGenerator
import com.mydocx.app.util.SessionManager
import kotlinx.coroutines.launch

class PublicProfileActivity : AppCompatActivity() {

    private var isFollowing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_public_profile)

        val session = SessionManager(this)
        val repo = ContentRepository(session)
        val userId = intent.getStringExtra("user_id") ?: return finish()

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        toolbar.setNavigationOnClickListener { finish() }

        if (userId == session.userId) {
            // Viewing your own profile via a link -> just reuse edit screen affordance later;
            // for now still render read-only using the same activity.
        }

        lifecycleScope.launch {
            val profile = repo.getProfile(userId) ?: return@launch
            findViewById<TextView>(R.id.tvFullName).text = profile.fullName
            findViewById<TextView>(R.id.tvUsername).text = "@${profile.username}"
            findViewById<TextView>(R.id.tvBio).text = profile.bio?.ifBlank { null } ?: "—"
            findViewById<TextView>(R.id.tvFollowersCount).text = profile.followersCount.toString()
            findViewById<TextView>(R.id.tvFollowingCount).text = profile.followingCount.toString()

            val avatarView = findViewById<android.widget.ImageView>(R.id.imgAvatar)
            if (!profile.avatarUrl.isNullOrEmpty()) {
                avatarView.load(profile.avatarUrl) { transformations(CircleCropTransformation()) }
            } else {
                avatarView.setImageBitmap(AvatarGenerator.generate(profile.avatarSeed ?: profile.username))
            }

            val linksContainer = findViewById<LinearLayout>(R.id.socialLinksContainer)
            linksContainer.removeAllViews()
            profile.socialLinks?.forEach { link ->
                val row = layoutInflater.inflate(R.layout.item_social_link, linksContainer, false)
                row.findViewById<TextView>(R.id.tvLinkLabel).text = link.label
                row.setOnClickListener { startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse(link.url))) }
                linksContainer.addView(row)
            }
        }

        findViewById<MaterialButton>(R.id.btnFollow).setOnClickListener {
            lifecycleScope.launch {
                isFollowing = repo.toggleFollow(userId, isFollowing)
                val btn = findViewById<MaterialButton>(R.id.btnFollow)
                btn.text = if (isFollowing) getString(R.string.following) else getString(R.string.follow)
            }
        }

        findViewById<View_>(R.id.btnFollowers).let {
            it.setOnClickListener {
                startActivity(Intent(this@PublicProfileActivity, UserListActivity::class.java).apply {
                    putExtra("user_id", userId); putExtra("mode", "followers")
                })
            }
        }
        findViewById<View_>(R.id.btnFollowing).let {
            it.setOnClickListener {
                startActivity(Intent(this@PublicProfileActivity, UserListActivity::class.java).apply {
                    putExtra("user_id", userId); putExtra("mode", "following")
                })
            }
        }
    }
}

// Small typealias so the two "View" lookups above type-check cleanly.
private typealias View_ = android.view.View
