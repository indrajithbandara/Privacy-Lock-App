package com.example.security

import android.content.Context
import android.os.SystemClock
import android.provider.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.UUID

data class AuthenticationResult(val accepted: Boolean, val retryAfterSeconds: Long = 0)

object AttemptDelayPolicy {
    fun delayMillis(attempt: Int, threshold: Int): Long {
        val limit = threshold.coerceIn(1, 20)
        val count = attempt.coerceAtLeast(1)
        return if (count >= limit) {
            (30_000L shl (count - limit).coerceIn(0, 4)).coerceAtMost(300_000L)
        } else {
            (1_000L shl (count - 1).coerceIn(0, 4))
        }
    }
}

/** Shared by the app and lock screen. Records the attempt before credential verification. */
object AuthenticationGate {
    private val mutex = Mutex()
    private val processId = UUID.randomUUID().toString()

    suspend fun authenticate(
        context: Context,
        threshold: Int,
        verify: suspend () -> Boolean
    ): AuthenticationResult = mutex.withLock {
        withContext(Dispatchers.IO) {
            val prefs = context.applicationContext.getSharedPreferences("authentication_attempts_v1", Context.MODE_PRIVATE)
            val boot = try {
                "boot:" + Settings.Global.getInt(context.contentResolver, Settings.Global.BOOT_COUNT)
            } catch (_: Exception) {
                "process:" + processId
            }
            val now = SystemClock.elapsedRealtime()
            val count = prefs.getInt("attempts", 0).coerceIn(0, 100)
            val storedBoot = prefs.getString("boot", null)
            var until = prefs.getLong("until", 0)
            if (count > 0 && (storedBoot != boot || now < prefs.getLong("started", 0))) {
                // Never shorten a pending cooldown after a reboot or an unknown boot identifier.
                until = now + AttemptDelayPolicy.delayMillis(count, threshold)
                if (!prefs.edit().putString("boot", boot).putLong("started", now)
                        .putLong("until", until).commit()) return@withContext AuthenticationResult(false)
            }
            if (until > now) {
                return@withContext AuthenticationResult(false, (until - now + 999) / 1000)
            }
            val next = (count + 1).coerceAtMost(100)
            val delay = AttemptDelayPolicy.delayMillis(next, threshold)
            if (!prefs.edit().putInt("attempts", next).putString("boot", boot)
                    .putLong("started", now).putLong("until", now + delay).commit()) {
                return@withContext AuthenticationResult(false)
            }
            if (verify()) {
                // Failure to persist reset is not an authentication success.
                return@withContext AuthenticationResult(prefs.edit().clear().commit())
            }
            val remaining = (now + delay - SystemClock.elapsedRealtime()).coerceAtLeast(0)
            AuthenticationResult(false, (remaining + 999) / 1000)
        }
    }
}
