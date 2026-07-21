package com.mydocx.app.repo

import com.mydocx.app.network.RetrofitClient
import com.mydocx.app.network.SignInRequest
import com.mydocx.app.network.SignUpRequest
import com.mydocx.app.util.PinHasher
import com.mydocx.app.util.SessionManager

/**
 * MyDocx uses username + password (Supabase Auth requires an email under the
 * hood, so we transparently map username -> "<username>@mydocx.local"),
 * plus a second-factor 4-digit "red passkey" PIN stored hashed on the profile.
 */
class AuthRepository(private val session: SessionManager) {

    private val api get() = RetrofitClient.api

    private fun pseudoEmail(username: String) = "${username.lowercase().trim()}@mydocx.local"

    sealed class Result<out T> {
        data class Success<T>(val data: T) : Result<T>()
        data class Error(val message: String) : Result<Nothing>()
    }

    suspend fun register(fullName: String, username: String, password: String, pin: String): Result<Unit> {
        val resp = api.signUp(
            SignUpRequest(
                email = pseudoEmail(username),
                password = password,
                data = mapOf("full_name" to fullName, "username" to username)
            )
        )
        if (!resp.isSuccessful || resp.body()?.user == null) {
            return Result.Error(resp.errorBody()?.string() ?: "تعذر إنشاء الحساب")
        }
        val body = resp.body()!!
        val userId = body.user!!.id
        session.accessToken = body.access_token
        session.refreshToken = body.refresh_token
        session.userId = userId
        session.username = username

        // Store the hashed PIN + build a default identicon avatar seed on the profile row.
        // The `profiles` row itself is auto-created by a DB trigger on auth.users insert
        // (see supabase/schema.sql: handle_new_user()); here we just attach the PIN + seed.
        val salt = PinHasher.newSalt()
        val hash = PinHasher.hash(pin, salt)
        api.updateProfile(
            idFilter = "eq.$userId",
            body = mapOf("pin_hash" to hash, "pin_salt" to salt, "avatar_seed" to username),
            bearer = RetrofitClient.bearer(body.access_token)
        )
        session.pinVerified = true
        return Result.Success(Unit)
    }

    suspend fun login(username: String, password: String): Result<Unit> {
        val resp = api.signIn(SignInRequest(pseudoEmail(username), password))
        if (!resp.isSuccessful || resp.body()?.user == null) {
            return Result.Error("اسم المستخدم أو كلمة السر غير صحيحة")
        }
        val body = resp.body()!!
        session.accessToken = body.access_token
        session.refreshToken = body.refresh_token
        session.userId = body.user!!.id
        session.username = username
        session.pinVerified = false // must clear the red passkey each new login
        return Result.Success(Unit)
    }

    suspend fun verifyPin(pin: String): Result<Unit> {
        val userId = session.userId ?: return Result.Error("الجلسة منتهية")
        val resp = api.getProfile("eq.$userId", RetrofitClient.bearer(session.accessToken))
        val profile = resp.body()?.firstOrNull() ?: return Result.Error("تعذر التحقق")
        val hash = profile.pinHash
        val salt = profile.pinSalt
        if (hash == null || salt == null) return Result.Error("لم يتم إعداد مفتاح المرور بعد")
        return if (PinHasher.verify(pin, salt, hash)) {
            session.pinVerified = true
            Result.Success(Unit)
        } else {
            Result.Error("رمز المرور غير صحيح")
        }
    }

    fun logout() = session.clear()
}
