package com.mydocx.app.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri

/**
 * "ترجمة" button: opens the text in Google Translate.
 * Prefers the installed Google Translate app (via its translate intent);
 * falls back to translate.google.com in the browser if the app isn't installed.
 */
object TranslateHelper {

    fun translate(context: Context, text: String, targetLang: String = "en") {
        val trimmed = if (text.length > 4000) text.substring(0, 4000) else text
        val appIntent = Intent(Intent.ACTION_PROCESS_TEXT).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_PROCESS_TEXT, trimmed)
            putExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, true)
            setPackage("com.google.android.apps.translate")
        }
        try {
            context.startActivity(appIntent)
        } catch (e: ActivityNotFoundException) {
            val webUri: Uri = "https://translate.google.com/?sl=auto&tl=$targetLang&text=${Uri.encode(trimmed)}&op=translate".toUri()
            context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
        }
    }
}
