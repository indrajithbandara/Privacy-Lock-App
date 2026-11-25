package com.example.data

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.AtomicFile
import java.io.File
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

internal object DatabaseKeyStore {
    private const val ALIAS = "privacy_lock_database_v1"
    private val aad = "privacy-lock-database-v1".toByteArray(Charsets.UTF_8)

    private fun key(create: Boolean): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val existing = store.getKey(ALIAS, null)
        if (existing is SecretKey) return existing
        check(create && !store.containsAlias(ALIAS)) { "Database key is unavailable" }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
            init(KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build())
        }.generateKey()
    }

    @Synchronized
    fun loadOrCreate(context: Context, encryptedDatabaseExists: Boolean): ByteArray {
        val file = AtomicFile(File(context.noBackupFilesDir, "database_key_v1"))
        if (file.baseFile.exists()) {
            val blob = file.readFully()
            require(blob.size == 60) { "Invalid database key envelope" }
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key(false), GCMParameterSpec(128, blob.copyOfRange(0, 12)))
            cipher.updateAAD(aad)
            return cipher.doFinal(blob.copyOfRange(12, blob.size)).also { require(it.size == 32) }
        }
        check(!encryptedDatabaseExists) { "Existing encrypted database key is missing" }
        val raw = ByteArray(32).also(SecureRandom()::nextBytes)
        try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, key(true))
            cipher.updateAAD(aad)
            require(cipher.iv.size == 12)
            val blob = cipher.iv + cipher.doFinal(raw)
            val output = file.startWrite()
            try {
                output.write(blob)
                file.finishWrite(output)
            } catch (error: Exception) {
                file.failWrite(output)
                throw error
            }
            return raw
        } catch (error: Exception) {
            raw.fill(0)
            throw error
        }
    }
}
