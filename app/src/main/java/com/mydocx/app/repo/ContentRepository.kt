package com.mydocx.app.repo

import com.mydocx.app.model.*
import com.mydocx.app.network.RetrofitClient
import com.mydocx.app.util.SessionManager

class ContentRepository(private val session: SessionManager) {

    private val api get() = RetrofitClient.api
    private fun bearer() = RetrofitClient.bearer(session.accessToken)

    suspend fun fetchArticles(): List<Article> =
        api.getFeedArticles(bearer()).body().orEmpty()

    suspend fun fetchProjects(): List<ProjectPost> =
        api.getFeedProjects(bearer()).body().orEmpty()

    suspend fun publishArticle(title: String, body: String, coverUrl: String?): Article? {
        val userId = session.userId ?: return null
        val article = Article(authorId = userId, title = title, body = body, coverUrl = coverUrl)
        return api.createArticle(article, bearer()).body()?.firstOrNull()
    }

    suspend fun publishProject(
        name: String, description: String, repoUrl: String?,
        storagePath: String?, language: String?
    ): ProjectPost? {
        val userId = session.userId ?: return null
        val project = ProjectPost(
            authorId = userId, name = name, description = description,
            repoUrl = repoUrl, storagePath = storagePath, language = language
        )
        return api.createProject(project, bearer()).body()?.firstOrNull()
    }

    suspend fun toggleLike(contentType: ContentType, contentId: String, currentlyLiked: Boolean): Boolean {
        val userId = session.userId ?: return currentlyLiked
        return if (currentlyLiked) {
            api.unlike(userId, contentId, bearer())
            false
        } else {
            api.like(Like(userId, contentType.name, contentId), bearer())
            true
        }
    }

    suspend fun repost(contentType: ContentType, contentId: String) {
        val userId = session.userId ?: return
        api.repost(Repost(userId, contentType.name, contentId), bearer())
    }

    suspend fun report(contentType: ContentType, contentId: String, reason: String) {
        val userId = session.userId ?: return
        api.report(Report(userId, contentType.name, contentId, reason), bearer())
    }

    suspend fun toggleFollow(targetUserId: String, currentlyFollowing: Boolean): Boolean {
        val myId = session.userId ?: return currentlyFollowing
        return if (currentlyFollowing) {
            api.unfollow(myId, targetUserId, bearer())
            false
        } else {
            api.follow(Follow(myId, targetUserId), bearer())
            true
        }
    }

    suspend fun subscribeMonthly(email: String) {
        api.subscribeMonthly(
            NewsletterSubscription(email = email, userId = session.userId),
            bearer()
        )
    }

    suspend fun getProfile(userId: String): Profile? =
        api.getProfile("eq.$userId", bearer()).body()?.firstOrNull()

    suspend fun getProfileByUsername(username: String): Profile? =
        api.getProfileByUsername("eq.$username", bearer()).body()?.firstOrNull()

    suspend fun updateProfile(userId: String, fields: Map<String, Any?>): Profile? =
        api.updateProfile("eq.$userId", fields, bearer()).body()?.firstOrNull()
}
