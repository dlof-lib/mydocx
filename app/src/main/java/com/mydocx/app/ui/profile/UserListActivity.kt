package com.mydocx.app.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mydocx.app.R
import com.mydocx.app.model.Profile
import com.mydocx.app.network.RetrofitClient
import com.mydocx.app.repo.ContentRepository
import com.mydocx.app.util.AvatarGenerator
import com.mydocx.app.util.SessionManager
import kotlinx.coroutines.launch

/** Shows a user's followers or following list (see string mode = "followers" | "following"). */
class UserListActivity : AppCompatActivity() {

    private lateinit var session: SessionManager
    private lateinit var repo: ContentRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_list)
        session = SessionManager(this)
        repo = ContentRepository(session)

        val userId = intent.getStringExtra("user_id") ?: return finish()
        val mode = intent.getStringExtra("mode") ?: "followers"

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = if (mode == "followers") getString(R.string.followers) else getString(R.string.followed)
        toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        toolbar.setNavigationOnClickListener { finish() }

        val recycler = findViewById<RecyclerView>(R.id.recyclerUsers)
        recycler.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            // NOTE: fetching the follow edges and resolving each into a full Profile.
            // For heavy production use, replace with a single PostgREST embedded-resource
            // query (follows?select=*,profile:profiles(*)) to avoid N+1 requests.
            val bearer = RetrofitClient.bearer(session.accessToken)
            val edges = RetrofitClient.api.getFollowers(userId, bearer).body().orEmpty()
            val ids = if (mode == "followers") edges.map { it.followerId } else edges.map { it.followingId }
            val profiles = ids.mapNotNull { repo.getProfile(it) }
            recycler.adapter = UserAdapter(profiles)
        }
    }

    inner class UserAdapter(private val users: List<Profile>) : RecyclerView.Adapter<UserAdapter.VH>() {
        inner class VH(v: android.view.View) : RecyclerView.ViewHolder(v)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
            VH(LayoutInflater.from(parent.context).inflate(R.layout.item_user_row, parent, false))

        override fun getItemCount() = users.size

        override fun onBindViewHolder(holder: VH, position: Int) {
            val u = users[position]
            holder.itemView.findViewById<TextView>(R.id.tvFullName).text = u.fullName
            holder.itemView.findViewById<TextView>(R.id.tvUsername).text = "@${u.username}"
            val avatarView = holder.itemView.findViewById<android.widget.ImageView>(R.id.imgAvatarView)
            if (!u.avatarUrl.isNullOrEmpty()) {
                coil.Coil.imageLoader(this@UserListActivity).enqueue(
                    coil.request.ImageRequest.Builder(this@UserListActivity)
                        .data(u.avatarUrl).target(avatarView)
                        .transformations(coil.transform.CircleCropTransformation())
                        .build()
                )
            } else {
                avatarView.setImageBitmap(AvatarGenerator.generate(u.avatarSeed ?: u.username))
            }
            holder.itemView.findViewById<ImageButton>(R.id.btnFollow).setOnClickListener {
                lifecycleScope.launch { repo.toggleFollow(u.id, currentlyFollowing = false) }
            }
            holder.itemView.setOnClickListener {
                val i = android.content.Intent(this@UserListActivity, PublicProfileActivity::class.java)
                i.putExtra("user_id", u.id)
                startActivity(i)
            }
        }
    }
}
