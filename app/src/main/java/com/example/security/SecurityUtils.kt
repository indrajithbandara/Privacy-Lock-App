package com.example.security

import java.security.MessageDigest
import java.security.SecureRandom

object SecurityUtils {

    private val secureRandom = SecureRandom()

    /**
     * Hashes a PIN or Password using SHA-256 with a local salt.
     */
    fun hashPin(pin: String, salt: String = "PrivacyLockSaltEnterprise"): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val combined = pin + salt
            val hashBytes = digest.digest(combined.toByteArray(Charsets.UTF_8))
            hashBytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            // Safe fallback (never crash the app, but log or handle gracefully)
            pin.hashCode().toString()
        }
    }

    /**
     * Generates a 4-digit secret key for Stealth Dial mode.
     */
    fun generateSecretDialCode(): String {
        val number = 1000 + secureRandom.nextInt(9000)
        return "#$number"
    }
}
