package com.mydocx.app.ui.feed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.button.MaterialButtonToggleGroup
import com.mydocx.app.R
import com.mydocx.app.model.ContentType
import com.mydocx.app.model.ProjectPost
import com.mydocx.app.repo.ContentRepository
import com.mydocx.app.ui.profile.PublicProfileActivity
import com.mydocx.app.util.SessionManager
import com.mydocx.app.util.TranslateHelper
import kotlinx.coroutines.launch

class FeedFragment : Fragment(), FeedActionListener {

    private lateinit var repo: ContentRepository
    private lateinit var session: SessionManager
    private var showingArticles = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_feed, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        session = SessionManager(requireContext())
        repo = ContentRepository(session)

        val recycler = view.findViewById<RecyclerView>(R.id.recyclerFeed)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        val swipe = view.findViewById<SwipeRefreshLayout>(R.id.swipeRefresh)
        val toggle = view.findViewById<MaterialButtonToggleGroup>(R.id.toggleGroup)

        toggle.check(R.id.btnTabArticles)
        toggle.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            showingArticles = checkedId == R.id.btnTabArticles
            loadFeed(recycler)
        }

        swipe.setOnRefreshListener { loadFeed(recycler) { swipe.isRefreshing = false } }
        loadFeed(recycler)
    }

    private fun loadFeed(recycler: RecyclerView, onDone: (() -> Unit)? = null) {
        lifecycleScope.launch {
            val items: List<FeedItem> = if (showingArticles) {
                repo.fetchArticles().map { FeedItem.ArticleItem(it) }
            } else {
                repo.fetchProjects().map { FeedItem.ProjectItem(it) }
            }
            recycler.adapter = FeedAdapter(items.toMutableList(), session.userId, this@FeedFragment)
            onDone?.invoke()
        }
    }

    override fun onLike(contentType: ContentType, id: String, currentlyLiked: Boolean, position: Int) {
        lifecycleScope.launch { repo.toggleLike(contentType, id, currentlyLiked) }
    }

    override fun onRepost(contentType: ContentType, id: String) {
        lifecycleScope.launch {
            repo.repost(contentType, id)
            Toast.makeText(requireContext(), getString(R.string.repost), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onReport(contentType: ContentType, id: String) {
        val input = EditText(requireContext())
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.report_reason_title)
            .setView(input)
            .setPositiveButton(R.string.report) { _, _ ->
                val reason = input.text.toString().ifBlank { "غير محدد" }
                lifecycleScope.launch { repo.report(contentType, id, reason) }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    override fun onFollow(authorId: String, currentlyFollowing: Boolean) {
        lifecycleScope.launch { repo.toggleFollow(authorId, currentlyFollowing) }
    }

    override fun onTranslate(text: String) {
        TranslateHelper.translate(requireContext(), text)
    }

    override fun onDownloadStructured(project: ProjectPost) {
        val url = project.storagePath
        if (url.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "لا يوجد ملف مرفق لهذا المشروع", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
        startActivity(intent)
    }

    override fun onOpenProfile(userId: String) {
        val intent = android.content.Intent(requireContext(), PublicProfileActivity::class.java)
        intent.putExtra("user_id", userId)
        startActivity(intent)
    }
}
