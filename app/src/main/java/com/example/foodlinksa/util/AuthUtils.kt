package com.example.foodlinksa.util

import org.mindrot.jbcrypt.BCrypt

object AuthUtils {
    /**
     * Hashes the password using BCrypt.
     */
    fun hashPassword(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt())
    }

    /**
     * Checks if the plain password matches the hashed password.
     */
    fun checkPassword(password: String, hashed: String): Boolean {
        return try {
            BCrypt.checkpw(password, hashed)
        } catch (e: Exception) {
            false
        }
    }
}
