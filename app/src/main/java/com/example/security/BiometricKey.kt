package com.example.security

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

/** Per-use biometric-bound Keystore operation; only PIN-authorized settings may enroll it. */
object BiometricKey {
    private const val ALIAS = "privacy_lock_biometric_v1"

    private fun store() = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

    fun enroll() {
        remove()
        val spec = KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setUserAuthenticationRequired(true)
            .setInvalidatedByBiometricEnrollment(true)
        if (Build.VERSION.SDK_INT >= 30) {
            spec.setUserAuthenticationParameters(0, KeyProperties.AUTH_BIOMETRIC_STRONG)
        } else {
            @Suppress("DEPRECATION")
            spec.setUserAuthenticationValidityDurationSeconds(-1)
        }
        KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
            init(spec.build())
        }.generateKey()
    }

    fun remove() {
        store().apply { if (containsAlias(ALIAS)) deleteEntry(ALIAS) }
    }

    fun newAuthenticationCipher(): Cipher? {
        return try {
            val key = store().getKey(ALIAS, null) as? SecretKey ?: return null
            Cipher.getInstance("AES/GCM/NoPadding").apply { init(Cipher.ENCRYPT_MODE, key) }
        } catch (_: Exception) { null }
    }
}
