package com.misuper.backend.security

import at.favre.lib.crypto.bcrypt.BCrypt

class PasswordHasher(private val cost: Int = 12) {

    fun hash(password: String): String {
        return BCrypt.withDefaults().hashToString(cost, password.toCharArray())
    }

    fun verify(password: String, hash: String): Boolean {
        return BCrypt.verifyer().verify(password.toCharArray(), hash).verified
    }
}
