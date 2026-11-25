package com.example
import com.example.security.AttemptDelayPolicy
import org.junit.Assert.*
import org.junit.Test

class AttemptDelayPolicyTest {
    @Test fun progressivelyIncreasesAndCapsCooldown() {
        assertEquals(1000L, AttemptDelayPolicy.delayMillis(1, 5))
        assertEquals(2000L, AttemptDelayPolicy.delayMillis(2, 5))
        assertEquals(30000L, AttemptDelayPolicy.delayMillis(5, 5))
        assertEquals(60000L, AttemptDelayPolicy.delayMillis(6, 5))
        assertEquals(300000L, AttemptDelayPolicy.delayMillis(100, 5))
    }
    @Test fun thresholdIsBounded() {
        assertEquals(30000L, AttemptDelayPolicy.delayMillis(1, 0))
        assertTrue(AttemptDelayPolicy.delayMillis(1, 100) > 0)
    }
}
