package com.mydocx.app.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import kotlin.math.min

/**
 * Draws a unique default profile avatar entirely in code (no external assets),
 * deterministically seeded from the user's username/id so every account gets
 * a distinct but reproducible identicon-style avatar the moment it's created,
 * before the user ever uploads a real photo.
 *
 * Style: a rounded "paper" tile in one of the brand palettes, a bold initial
 * letter, and a thin geometric accent ring — matching the MyDocx visual identity.
 */
object AvatarGenerator {

    private val palettes = listOf(
        intArrayOf(0xFF151F2C.toInt(), 0xFFE23B3B.toInt()), // ink + signal red
        intArrayOf(0xFF1D2A3A.toInt(), 0xFFD9A441.toInt()), // ink + gold
        intArrayOf(0xFF2A3B50.toInt(), 0xFF3BA776.toInt()), // steel + green
        intArrayOf(0xFF5B3A29.toInt(), 0xFFE9A23B.toInt()), // brown + amber
        intArrayOf(0xFF3A2A5B.toInt(), 0xFFB37FE2.toInt())  // plum + lilac
    )

    fun seedFor(usernameOrId: String): Int = usernameOrId.sumOf { it.code }

    fun generate(seedKey: String, sizePx: Int = 512): Bitmap {
        val seed = seedFor(seedKey)
        val palette = palettes[seed % palettes.size]
        val bg = palette[0]
        val accent = palette[1]
        val initial = seedKey.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "M"

        val bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = bg }
        val accentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accent
            style = Paint.Style.STROKE
            strokeWidth = sizePx * 0.045f
        }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFF7F5F0.toInt()
            textSize = sizePx * 0.42f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }

        val radius = sizePx * 0.22f
        val rect = RectF(0f, 0f, sizePx.toFloat(), sizePx.toFloat())
        canvas.drawRoundRect(rect, radius, radius, bgPaint)

        // Geometric accent ring, offset like a wax seal
        val ringInset = sizePx * 0.09f
        canvas.drawCircle(
            sizePx - ringInset, ringInset,
            sizePx * 0.10f, accentPaint
        )

        // Initial letter, vertically centered
        val textY = sizePx / 2f - (textPaint.descent() + textPaint.ascent()) / 2f
        canvas.drawText(initial, sizePx / 2f, textY, textPaint)

        return bmp
    }

    /** Saves the generated avatar as a shareable PNG and returns a content:// Uri for download/share. */
    fun downloadAsPng(context: Context, seedKey: String): android.net.Uri {
        val bmp = generate(seedKey)
        val dir = File(context.cacheDir, "downloads").apply { mkdirs() }
        val file = File(dir, "mydocx_avatar_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { out -> bmp.compress(Bitmap.CompressFormat.PNG, 100, out) }
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    fun squareSize(w: Int, h: Int) = min(w, h)
}
