package com.md.mypuzzleapp.util

import java.security.MessageDigest
import java.util.*

object EmailUtils {
    /**
     * Normalizes and hashes an email for privacy
     * @return Pair of (normalizedEmail, hashedEmail) or null if invalid
     */
    fun processEmail(email: String): Pair<String, String>? {
        val normalizedEmail = email.trim().lowercase(Locale.US)
        
        // Basic email format validation
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.[a-z]+"
        if (!normalizedEmail.matches(Regex(emailPattern))) {
            return null
        }
        
        // Hash the email using SHA-256
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(normalizedEmail.toByteArray())
        val hashedEmail = hashBytes.fold("") { str, it -> str + "%02x".format(it) }
        
        return normalizedEmail to "hashed_$hashedEmail"
    }
    
    /**
     * Validates if an email is in correct format
     */
    fun isValidEmail(email: String): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.[a-z]+"
        return email.matches(Regex(emailPattern))
    }
}
