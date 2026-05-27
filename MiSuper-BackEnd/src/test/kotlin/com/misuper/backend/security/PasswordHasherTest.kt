package com.misuper.backend.security

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PasswordHasherTest {
    @Test
    fun verifiesPasswordAgainstGeneratedHash() {
        val hasher = PasswordHasher(cost = 4)
        val hash = hasher.hash("MiPassword123")

        assertTrue(hasher.verify("MiPassword123", hash))
        assertFalse(hasher.verify("otra-password", hash))
    }
}
