package com.mydocx.app.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mydocx.app.R
import com.mydocx.app.ui.feed.FeedFragment
import com.mydocx.app.ui.notifications.NotificationsFragment
import com.mydocx.app.ui.profile.ProfileFragment
import com.mydocx.app.ui.publish.PublishChooserActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        showFragment(FeedFragment())

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> { showFragment(FeedFragment()); true }
                R.id.nav_explore -> { showFragment(FeedFragment()); true }
                R.id.nav_publish -> {
                    startActivity(Intent(this, PublishChooserActivity::class.java))
                    false // don't visually select the publish tab; it's an action, not a screen
                }
                R.id.nav_notifications -> { showFragment(NotificationsFragment()); true }
                R.id.nav_profile -> { showFragment(ProfileFragment()); true }
                else -> false
            }
        }
    }

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
