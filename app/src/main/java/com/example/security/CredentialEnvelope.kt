package com.example.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Encrypts the credential verifier with an app-scoped Android Keystore key. */
object CredentialEnvelope {
    private const val ALIAS = "privacy_lock_credential_v1"
    private const val PREFIX = "enc1:"
    private val aad = "privacy-lock-credential-v1".toByteArray(Charsets.UTF_8)
    private val keyLock = Any()

    fun isWrapped(record: String): Boolean = record.startsWith(PREFIX)

    private fun key(create: Boolean): SecretKey = synchronized(keyLock) {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val existing = store.getKey(ALIAS, null)
        if (existing is SecretKey) return@synchronized existing
        check(create && !store.containsAlias(ALIAS)) { "Credential key unavailable" }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(KeyGenParameterSpec.Builder(ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build())
        generator.generateKey()
    }

    fun wrap(record: String): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key(create = true))
        cipher.updateAAD(aad)
        val encrypted = cipher.doFinal(record.toByteArray(Charsets.UTF_8))
        return PREFIX + Base64.encodeToString(cipher.iv, Base64.NO_WRAP) + ":" +
            Base64.encodeToString(encrypted, Base64.NO_WRAP)
    }

    fun unwrap(record: String): String? {
        if (!isWrapped(record)) return record // Supported legacy migration only.
        return try {
            val parts = record.split(':')
            if (parts.size != 3) return null
            val iv = Base64.decode(parts[1], Base64.NO_WRAP)
            if (iv.size != 12) return null
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            // Never regenerate a missing key when decrypting existing credentials.
            cipher.init(Cipher.DECRYPT_MODE, key(create = false), GCMParameterSpec(128, iv))
            cipher.updateAAD(aad)
            cipher.doFinal(Base64.decode(parts[2], Base64.NO_WRAP)).toString(Charsets.UTF_8)
        } catch (_: Exception) { null }
    }
}
