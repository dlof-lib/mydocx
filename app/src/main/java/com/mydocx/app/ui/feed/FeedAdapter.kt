package com.mydocx.app.ui.feed

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.mydocx.app.R
import com.mydocx.app.model.Article
import com.mydocx.app.model.ContentType
import com.mydocx.app.model.ProjectPost
import com.mydocx.app.util.AvatarGenerator

sealed class FeedItem {
    data class ArticleItem(val article: Article) : FeedItem()
    data class ProjectItem(val project: ProjectPost) : FeedItem()
}

interface FeedActionListener {
    fun onLike(contentType: ContentType, id: String, currentlyLiked: Boolean, position: Int)
    fun onRepost(contentType: ContentType, id: String)
    fun onReport(contentType: ContentType, id: String)
    fun onFollow(authorId: String, currentlyFollowing: Boolean)
    fun onTranslate(text: String)
    fun onDownloadStructured(project: ProjectPost)
    fun onOpenProfile(userId: String)
}

class FeedAdapter(
    private val items: MutableList<FeedItem>,
    private val myUserId: String?,
    private val listener: FeedActionListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val likedIds = mutableSetOf<String>()

    private val TYPE_ARTICLE = 1
    private val TYPE_PROJECT = 2

    override fun getItemViewType(position: Int) = when (items[position]) {
        is FeedItem.ArticleItem -> TYPE_ARTICLE
        is FeedItem.ProjectItem -> TYPE_PROJECT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_ARTICLE) {
            ArticleVH(inflater.inflate(R.layout.item_article, parent, false))
        } else {
            ProjectVH(inflater.inflate(R.layout.item_project, parent, false))
        }
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is FeedItem.ArticleItem -> (holder as ArticleVH).bind(item.article, position)
            is FeedItem.ProjectItem -> (holder as ProjectVH).bind(item.project, position)
        }
    }

    private fun loadAvatar(iv: ImageView, seed: String?, url: String?) {
        if (!url.isNullOrEmpty()) {
            iv.load(url) { transformations(CircleCropTransformation()) }
        } else {
            iv.setImageBitmap(AvatarGenerator.generate(seed ?: "M"))
        }
    }

    inner class ArticleVH(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        fun bind(a: Article, position: Int) {
            val author = a.author
            itemView.findViewById<TextView>(R.id.tvAuthorName).text = author?.fullName ?: "مستخدم"
            itemView.findViewById<TextView>(R.id.tvUsernameTime).text = "@${author?.username ?: ""}"
            itemView.findViewById<TextView>(R.id.tvTitle).text = a.title
            itemView.findViewById<TextView>(R.id.tvBody).text = a.body
            loadAvatar(itemView.findViewById(R.id.imgAvatarView), author?.avatarSeed ?: author?.username, author?.avatarUrl)

            val id = a.id ?: return
            val liked = likedIds.contains(id)
            itemView.findViewById<ImageButton>(R.id.btnLike).setImageResource(
                if (liked) R.drawable.ic_like_filled else R.drawable.ic_like_outline
            )
            itemView.findViewById<TextView>(R.id.tvLikeCount).text = a.likesCount.toString()
            itemView.findViewById<TextView>(R.id.tvRepostCount).text = a.repostsCount.toString()

            itemView.findViewById<ImageButton>(R.id.btnLike).setOnClickListener {
                val nowLiked = !liked
                if (nowLiked) likedIds.add(id) else likedIds.remove(id)
                listener.onLike(ContentType.article, id, liked, position)
            }
            itemView.findViewById<ImageButton>(R.id.btnRepost).setOnClickListener {
                listener.onRepost(ContentType.article, id)
            }
            itemView.findViewById<ImageButton>(R.id.btnReport).setOnClickListener {
                listener.onReport(ContentType.article, id)
            }
            itemView.findViewById<ImageButton>(R.id.btnTranslate).setOnClickListener {
                listener.onTranslate(a.title + "\n\n" + a.body)
            }
            itemView.findViewById<ImageButton>(R.id.btnFollow).setOnClickListener {
                author?.id?.let { listener.onFollow(it, false) }
            }
            itemView.setOnClickListener { author?.id?.let { listener.onOpenProfile(it) } }
        }
    }

    inner class ProjectVH(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        fun bind(p: ProjectPost, position: Int) {
            val author = p.author
            itemView.findViewById<TextView>(R.id.tvAuthorName).text = author?.fullName ?: "مستخدم"
            itemView.findViewById<TextView>(R.id.tvUsernameTime).text = "@${author?.username ?: ""}"
            itemView.findViewById<TextView>(R.id.tvProjectName).text = p.name
            itemView.findViewById<TextView>(R.id.tvDescription).text = p.description
            itemView.findViewById<TextView>(R.id.tvLanguageBadge).text = p.language ?: "Code"
            loadAvatar(itemView.findViewById(R.id.imgAvatarView), author?.avatarSeed ?: author?.username, author?.avatarUrl)

            val id = p.id ?: return
            val liked = likedIds.contains(id)
            itemView.findViewById<ImageButton>(R.id.btnLike).setImageResource(
                if (liked) R.drawable.ic_like_filled else R.drawable.ic_like_outline
            )
            itemView.findViewById<TextView>(R.id.tvLikeCount).text = p.likesCount.toString()
            itemView.findViewById<TextView>(R.id.tvRepostCount).text = p.repostsCount.toString()

            itemView.findViewById<ImageButton>(R.id.btnLike).setOnClickListener {
                val nowLiked = !liked
                if (nowLiked) likedIds.add(id) else likedIds.remove(id)
                listener.onLike(ContentType.project, id, liked, position)
            }
            itemView.findViewById<ImageButton>(R.id.btnRepost).setOnClickListener {
                listener.onRepost(ContentType.project, id)
            }
            itemView.findViewById<ImageButton>(R.id.btnReport).setOnClickListener {
                listener.onReport(ContentType.project, id)
            }
            itemView.findViewById<ImageButton>(R.id.btnTranslate).setOnClickListener {
                listener.onTranslate(p.name + "\n\n" + p.description)
            }
            itemView.findViewById<ImageButton>(R.id.btnFollow).setOnClickListener {
                author?.id?.let { listener.onFollow(it, false) }
            }
            itemView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnDownloadStructured)
                .setOnClickListener { listener.onDownloadStructured(p) }
            itemView.setOnClickListener { author?.id?.let { listener.onOpenProfile(it) } }
        }
    }
}
