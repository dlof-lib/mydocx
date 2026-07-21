package com.mydocx.app.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import coil.load
import coil.transform.CircleCropTransformation
import com.mydocx.app.R
import com.mydocx.app.repo.ContentRepository
import com.mydocx.app.ui.feed.FeedActionListener
import com.mydocx.app.util.AvatarGenerator
import com.mydocx.app.util.SessionManager
import kotlinx.coroutines.launch

/** The signed-in user's own profile: bio, followers/following, special thanks,
 *  social links, monthly-email subscription block, reposts, and avatar download. */
class ProfileFragment : Fragment() {

    private lateinit var session: SessionManager
    private lateinit var repo: ContentRepository

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_profile, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        session = SessionManager(requireContext())
        repo = ContentRepository(session)
        val myId = session.userId ?: return

        lifecycleScope.launch {
            val profile = repo.getProfile(myId) ?: return@launch

            view.findViewById<TextView>(R.id.tvFullName).text = profile.fullName
            view.findViewById<TextView>(R.id.tvUsername).text = "@${profile.username}"
            view.findViewById<TextView>(R.id.tvBio).text =
                profile.bio?.ifBlank { null } ?: "أضف نبذة عنك من تعديل الملف الشخصي"
            view.findViewById<TextView>(R.id.tvSpecialThanks).text =
                profile.specialThanks?.ifBlank { null } ?: "—"
            view.findViewById<TextView>(R.id.tvFollowersCount).text = profile.followersCount.toString()
            view.findViewById<TextView>(R.id.tvFollowingCount).text = profile.followingCount.toString()

            val avatarView = view.findViewById<ImageView>(R.id.imgAvatar)
            if (!profile.avatarUrl.isNullOrEmpty()) {
                avatarView.load(profile.avatarUrl) { transformations(CircleCropTransformation()) }
            } else {
                avatarView.setImageBitmap(AvatarGenerator.generate(profile.avatarSeed ?: profile.username))
            }

            val linksContainer = view.findViewById<LinearLayout>(R.id.socialLinksContainer)
            linksContainer.removeAllViews()
            profile.socialLinks?.forEach { link ->
                val row = layoutInflater.inflate(R.layout.item_social_link, linksContainer, false)
                row.findViewById<TextView>(R.id.tvLinkLabel).text = link.label
                row.setOnClickListener {
                    startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse(link.url)))
                }
                linksContainer.addView(row)
            }
        }

        view.findViewById<View>(R.id.btnDownloadAvatar).setOnClickListener {
            val seed = session.username ?: "mydocx"
            val uri = AvatarGenerator.downloadAsPng(requireContext(), seed)
            Toast.makeText(requireContext(), "تم حفظ الأيقونة", Toast.LENGTH_SHORT).show()
            val share = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(share, getString(R.string.download_avatar)))
        }

        view.findViewById<View>(R.id.btnEditProfile).setOnClickListener {
            startActivity(Intent(requireContext(), EditProfileActivity::class.java))
        }

        view.findViewById<View>(R.id.btnFollowers).setOnClickListener {
            startActivity(Intent(requireContext(), UserListActivity::class.java).apply {
                putExtra("user_id", myId); putExtra("mode", "followers")
            })
        }
        view.findViewById<View>(R.id.btnFollowing).setOnClickListener {
            startActivity(Intent(requireContext(), UserListActivity::class.java).apply {
                putExtra("user_id", myId); putExtra("mode", "following")
            })
        }

        view.findViewById<Button>(R.id.btnSubscribe).setOnClickListener {
            val email = view.findViewById<EditText>(R.id.inputSubscribeEmail).text?.toString()?.trim().orEmpty()
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(requireContext(), "أدخل بريدًا إلكترونيًا صحيحًا", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            lifecycleScope.launch {
                repo.subscribeMonthly(email)
                Toast.makeText(requireContext(), "تم الاشتراك في النشرة الشهرية", Toast.LENGTH_SHORT).show()
            }
        }

        // Reposts section reuses the feed adapter/list logic; wire a dedicated
        // `getUserReposts(userId)` call in ContentRepository if you want it fully live.
    }
}
