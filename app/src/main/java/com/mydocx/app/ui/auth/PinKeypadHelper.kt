package com.mydocx.app.ui.auth

import android.app.Activity
import android.view.View
import android.widget.Button
import android.widget.EditText
import com.mydocx.app.R

/**
 * Wires the on-screen numeric keypad + 4 dot indicators used by both
 * PinSetupActivity and PinEntryActivity, so the "red passkey" UI stays
 * in exactly one place.
 */
class PinKeypadHelper(
    private val activity: Activity,
    private val onComplete: (String) -> Unit
) {
    private val dots: List<View> = listOf(
        activity.findViewById(R.id.dot1),
        activity.findViewById(R.id.dot2),
        activity.findViewById(R.id.dot3),
        activity.findViewById(R.id.dot4)
    )
    private val hiddenInput: EditText = activity.findViewById(R.id.hiddenPinInput)
    private var current = StringBuilder()

    init {
        val keyIds = intArrayOf(
            R.id.key0, R.id.key1, R.id.key2, R.id.key3, R.id.key4,
            R.id.key5, R.id.key6, R.id.key7, R.id.key8, R.id.key9
        )
        val digits = "0123456789"
        for (i in keyIds.indices) {
            activity.findViewById<Button>(keyIds[i]).setOnClickListener { onDigit(digits[i]) }
        }
        activity.findViewById<Button>(R.id.keyBack).setOnClickListener { onBackspace() }
    }

    private fun onDigit(d: Char) {
        if (current.length >= 4) return
        current.append(d)
        refresh()
        if (current.length == 4) {
            val pin = current.toString()
            onComplete(pin)
        }
    }

    private fun onBackspace() {
        if (current.isNotEmpty()) {
            current.deleteCharAt(current.length - 1)
            refresh()
        }
    }

    fun reset() {
        current.clear()
        refresh()
    }

    private fun refresh() {
        for (i in dots.indices) {
            dots[i].setBackgroundResource(
                if (i < current.length) R.drawable.pin_dot_filled else R.drawable.pin_dot_empty
            )
        }
    }
}
