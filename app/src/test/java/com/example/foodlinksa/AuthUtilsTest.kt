package com.example.foodlinksa

import com.example.foodlinksa.util.AuthUtils
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthUtilsTest {

    @Test
    fun testPasswordHashing() {
        val password = "mySecurePassword"
        val hash = AuthUtils.hashPassword(password)
        
        assertTrue("Hash should match the password", AuthUtils.checkPassword(password, hash))
        assertFalse("Hash should not match wrong password", AuthUtils.checkPassword("wrongPassword", hash))
    }

    @Test
    fun testEmptyPasswordHashing() {
        val password = ""
        val hash = AuthUtils.hashPassword(password)
        
        assertTrue("Hash should match empty password", AuthUtils.checkPassword(password, hash))
    }
}
