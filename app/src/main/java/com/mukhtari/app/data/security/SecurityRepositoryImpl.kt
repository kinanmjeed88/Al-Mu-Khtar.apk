package com.mukhtari.app.data.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.mukhtari.app.domain.repository.SecurityRepository
import java.security.MessageDigest
import java.security.spec.KeySpec
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import android.util.Base64

class SecurityRepositoryImpl(
    context: Context
) : SecurityRepository {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "security_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private fun hash(input: String): String {
        val salt = "MUKHTARI_SALT_123".toByteArray() // For simplicity in an offline local DB context, fixed or fetchable salt is common, but let's just make it significantly better than SHA256 alone.
        val spec: KeySpec = PBEKeySpec(input.toCharArray(), salt, 10000, 256)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val hash = factory.generateSecret(spec).encoded
        return Base64.encodeToString(hash, Base64.NO_WRAP)
    }

    override suspend fun setPin(pin: String) {
        sharedPreferences.edit().putString("pin_hash", hash(pin)).apply()
    }

    override suspend fun verifyPin(pin: String): Boolean {
        val storedHash = sharedPreferences.getString("pin_hash", null) ?: return false
        return storedHash == hash(pin)
    }

    override suspend fun isPinSet(): Boolean {
        return sharedPreferences.contains("pin_hash")
    }

    override suspend fun setSecurityQuestion(question: String, answer: String) {
        sharedPreferences.edit()
            .putString("security_question", question)
            .putString("security_answer_hash", hash(answer.lowercase().trim()))
            .apply()
    }

    override suspend fun verifySecurityAnswer(answer: String): Boolean {
        val storedHash = sharedPreferences.getString("security_answer_hash", null) ?: return false
        return storedHash == hash(answer.lowercase().trim())
    }

    override suspend fun enableBiometric(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("biometric_enabled", enabled).apply()
    }

    override suspend fun isBiometricEnabled(): Boolean {
        return sharedPreferences.getBoolean("biometric_enabled", false)
    }

    override suspend fun setAutoLockDuration(minutes: Int) {
        sharedPreferences.edit().putInt("auto_lock_duration", minutes).apply()
    }

    override suspend fun getAutoLockDuration(): Int {
        return sharedPreferences.getInt("auto_lock_duration", 0)
    }
}
