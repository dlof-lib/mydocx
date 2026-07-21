package com.mydocx.app.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.mydocx.app.R

/**
 * Placeholder notifications screen. Wire this to a `notifications` Supabase table
 * (fed by DB triggers on likes/reposts/follows/reports) when you're ready to expand it.
 */
class NotificationsFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val tv = TextView(requireContext())
        tv.text = "لا توجد إشعارات جديدة"
        tv.textSize = 15f
        tv.setPadding(32, 64, 32, 32)
        tv.gravity = android.view.Gravity.CENTER
        return tv
    }
}
