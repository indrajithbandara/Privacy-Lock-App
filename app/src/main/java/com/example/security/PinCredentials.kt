package com.example.security

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/** Versioned, independently salted PIN verifier. Legacy SHA-256 is read only for migration. */
object PinCredentials {
    private val random = SecureRandom()
    private const val SHA256_ROUNDS = 600_000
    private const val SHA1_ROUNDS = 1_300_000
    private val legacyPattern = Regex("[0-9a-f]{64}")

    fun isLegacy(record: String): Boolean = legacyPattern.matches(record)

    // API 24–25 have the SHA-1 PBKDF2 JCA factory, not the SHA-256 one.
    // The format records the selected algorithm; provider errors never trigger a fallback.
    fun create(pin: String, sha256Available: Boolean = true): String {
        require(pin.length == 6 && pin.all { it in '0'..'9' })
        val salt = ByteArray(16).also(random::nextBytes)
        val version = if (sha256Available) "p2s256" else "p2s1"
        val key = derive(pin, version, salt)
        return try { "$version:${salt.hex()}:${key.hex()}" } finally { key.fill(0) }
    }

    fun verify(pin: String, record: String): Boolean {
        if (pin.length != 6 || pin.any { it !in '0'..'9' }) return false
        return try {
            if (isLegacy(record)) {
                val old = MessageDigest.getInstance("SHA-256")
                    .digest((pin + "PrivacyLockSaltEnterprise").toByteArray(Charsets.UTF_8))
                return MessageDigest.isEqual(old, record.unhex())
            }
            val parts = record.split(':')
            if (parts.size != 3 || parts[0] !in setOf("p2s256", "p2s1") ||
                parts[1].length != 32 || parts[2].length != 64) return false
            val actual = derive(pin, parts[0], parts[1].unhex())
            try { MessageDigest.isEqual(actual, parts[2].unhex()) } finally { actual.fill(0) }
        } catch (_: Exception) { false }
    }

    private fun derive(pin: String, version: String, salt: ByteArray): ByteArray {
        val algorithm = when (version) {
            "p2s256" -> "PBKDF2WithHmacSHA256"
            "p2s1" -> "PBKDF2WithHmacSHA1"
            else -> error("Unsupported credential version")
        }
        val chars = pin.toCharArray()
        val spec = PBEKeySpec(chars, salt, if (version == "p2s256") SHA256_ROUNDS else SHA1_ROUNDS, 256)
        chars.fill('\u0000')
        return try { SecretKeyFactory.getInstance(algorithm).generateSecret(spec).encoded }
        finally { spec.clearPassword() }
    }

    private fun ByteArray.hex(): String = joinToString("") { "%02x".format(it) }
    private fun String.unhex(): ByteArray {
        require(length % 2 == 0 && all { it in '0'..'9' || it in 'a'..'f' })
        return chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    }
}
