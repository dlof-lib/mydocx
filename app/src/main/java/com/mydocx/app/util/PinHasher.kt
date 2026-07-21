package com.mydocx.app.util

import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Hashes the 4-digit "red passkey" PIN with a random per-user salt (SHA-256).
 *
 * Note: for a production app, PIN verification should ultimately be checked
 * server-side (e.g. a Supabase Edge Function / Postgres function using pgcrypto),
 * since any check that lives only on-device can be bypassed on a rooted phone.
 * This client-side hasher is provided so the PIN is never stored or transmitted
 * as plain text, and is a reasonable starting point for the MVP.
 */
object PinHasher {

    fun newSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun hash(pin: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update((salt + pin).toByteArray(Charsets.UTF_8))
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    fun verify(pin: String, salt: String, expectedHash: String): Boolean =
        hash(pin, salt) == expectedHash
}
