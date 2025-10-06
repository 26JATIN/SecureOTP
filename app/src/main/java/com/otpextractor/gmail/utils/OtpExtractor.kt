package com.otpextractor.secureotp.utils

import android.util.Log
import java.util.regex.Pattern

object OtpExtractor {

    private const val TAG = "OtpExtractor"

    // Data class to hold OTP candidates with their priority
    private data class OtpCandidate(
        val value: String,
        val priority: Int,
        val source: String
    )

    // OTP patterns based on user's analysis - Simple and accurate!
    // Key insight: OTP always comes AFTER keywords (is, use, :, etc.)
    // Exception: "X is otp for Y" - X is before "for"
    // NUMERIC ONLY - More reliable, fewer false positives
    private val highPriorityPatterns = listOf(
        // Pattern 1: "X is/are otp/code for Y" - Capture X (BEFORE "for")
        // Examples: "6767 is otp for 3838" → 6767
        //           "2310990533 is otp for 6767" → 2310990533
        Pattern.compile("\\b([0-9]{3,12})\\s+(?:is|are)\\s+(?:otp|code|verification|pin|password|passcode|token)\\s+for\\b", Pattern.CASE_INSENSITIVE),
        
        // Pattern 2: "is/are/:" followed by OTP (AFTER keyword, no "for")
        // Examples: "Your OTP is 123456" → 123456
        //           "OTP: 456789" → 456789
        //           "bank send otp 123456" → 123456 (after implicit context)
        Pattern.compile("(?:is|are|:)\\s*([0-9]{3,12})\\b", Pattern.CASE_INSENSITIVE),
        
        // Pattern 3: "use/enter/type" followed by OTP
        // Examples: "please use 890227 to accept" → 890227
        //           "use OTP-890227" → 890227 (ignore OTP- prefix)
        Pattern.compile("(?:use|enter|type)\\s+(?:otp[\\s-])?([0-9]{3,12})\\b", Pattern.CASE_INSENSITIVE),
        
        // Pattern 4: OTP in brackets/quotes (common formatting)
        // Examples: "Your code [123456]" → 123456
        Pattern.compile("[\"'\\[\\(]([0-9]{3,12})[\"'\\]\\)]"),
        
        // Pattern 5: "to" followed by action and OTP
        // Examples: "to accept delivery 890227" → 890227
        Pattern.compile("to\\s+(?:accept|verify|confirm|login|signin|complete|proceed)\\s+\\w*\\s*([0-9]{3,12})\\b", Pattern.CASE_INSENSITIVE),
    )

    private val mediumPriorityPatterns = listOf(
        // Pattern 6: Keywords followed by OTP (with optional context words)
        // Examples: "verification code is 123456" → 123456
        //           "authentication code: 456789" → 456789
        Pattern.compile("(?:verification|authentication|authorization|access|security|login|confirmation)\\s+(?:code|otp|pin|token|password)?\\s*(?:is|:|are)?\\s*([0-9]{3,12})\\b", Pattern.CASE_INSENSITIVE),
        
        // Pattern 7: OTP with dashes or spaces (formatted codes)
        // Examples: "12-34-56" → 123456
        Pattern.compile("\\b([0-9]{2,4}[-\\s][0-9]{2,4}(?:[-\\s][0-9]{2,4})?)\\b"),
    )

    private val lowPriorityPatterns = listOf(
        // Pattern 8: Standalone numeric codes (only when strong OTP context exists)
        // Examples: "bank send otp 123456" → 123456
        //           "Your code 456789" → 456789
        Pattern.compile("\\b([0-9]{4,10})\\b"),
    )

    // Enhanced keywords for OTP context detection
    private val otpKeywords = listOf(
        "otp", "verification", "code", "pin", "password", "passcode", "token",
        "authenticate", "verify", "security", "login", "signin", "sign-in", "sign in",
        "confirm", "confirmation", "activation", "activate", "authorization", "authorize",
        "2fa", "two-factor", "two factor", "mfa", "multi-factor", "multi factor",
        "temporary", "one-time", "onetime", "one time",
        "access", "authentication", "validation", "validate"
    )

    // Patterns to identify phone numbers and other numbers to ignore
    // Note: We DON'T ignore 10-12 digit numbers in high priority patterns
    // since OTPs can be that long (like 2310990533)
    private val ignorePatterns = listOf(
        Pattern.compile("^[0-9]{13,}$"),       // 13+ digits (tracking, order numbers, etc.)
        Pattern.compile("^[0-9]{1,3}$"),       // Too short (1-3 digits)
        Pattern.compile("^(19|20)[0-9]{2}$"),  // Years (1900-2099)
    )

    /**
     * Intelligently extracts OTP from text with advanced pattern matching
     * Handles numeric and alphanumeric OTPs
     * Returns the most likely OTP code or null if none found
     */
    fun extractOtp(text: String): String? {
        if (text.isBlank()) return null

        Log.d(TAG, "Extracting OTP from: $text")

        val lowerText = text.lowercase()
        val hasOtpContext = otpKeywords.any { lowerText.contains(it) }

        val candidates = mutableListOf<OtpCandidate>()

        // Phase 1: Try high priority patterns (most specific)
        for ((index, pattern) in highPriorityPatterns.withIndex()) {
            val matcher = pattern.matcher(text)
            while (matcher.find()) {
                val match = if (matcher.groupCount() > 0) {
                    matcher.group(1)
                } else {
                    matcher.group(0)
                } ?: continue

                val cleanMatch = cleanMatch(match)
                
                if (!shouldIgnoreValue(cleanMatch)) {
                    candidates.add(OtpCandidate(
                        value = cleanMatch,
                        priority = 100 - index, // Higher number = higher priority
                        source = "High Priority Pattern ${index + 1}"
                    ))
                    Log.d(TAG, "High priority match: $cleanMatch from pattern ${index + 1}")
                }
            }
        }

        // If we found high priority matches, return the best one
        if (candidates.isNotEmpty()) {
            val best = candidates.maxByOrNull { it.priority }!!
            Log.d(TAG, "Returning OTP: ${best.value} (${best.source})")
            return best.value
        }

        // Phase 2: Try medium priority patterns (contextual)
        if (hasOtpContext) {
            for ((index, pattern) in mediumPriorityPatterns.withIndex()) {
                val matcher = pattern.matcher(text)
                while (matcher.find()) {
                    val match = if (matcher.groupCount() > 0) {
                        matcher.group(1)
                    } else {
                        matcher.group(0)
                    } ?: continue

                    val cleanMatch = cleanMatch(match)
                    
                    if (!shouldIgnoreValue(cleanMatch)) {
                        candidates.add(OtpCandidate(
                            value = cleanMatch,
                            priority = 50 - index,
                            source = "Medium Priority Pattern ${index + 1}"
                        ))
                        Log.d(TAG, "Medium priority match: $cleanMatch from pattern ${index + 1}")
                    }
                }
            }

            if (candidates.isNotEmpty()) {
                val best = candidates.maxByOrNull { it.priority }!!
                Log.d(TAG, "Returning OTP: ${best.value} (${best.source})")
                return best.value
            }
        }

        // Phase 3: Try low priority patterns (fallback, only with strong OTP context)
        if (hasOtpContext) {
            for ((index, pattern) in lowPriorityPatterns.withIndex()) {
                val matcher = pattern.matcher(text)
                while (matcher.find()) {
                    val match = if (matcher.groupCount() > 0) {
                        matcher.group(1)
                    } else {
                        matcher.group(0)
                    } ?: continue

                    val cleanMatch = cleanMatch(match)
                    
                    // Be more selective with low priority patterns
                    if (!shouldIgnoreValue(cleanMatch) && isValidOtpLength(cleanMatch)) {
                        candidates.add(OtpCandidate(
                            value = cleanMatch,
                            priority = 10 - index,
                            source = "Low Priority Pattern ${index + 1}"
                        ))
                        Log.d(TAG, "Low priority match: $cleanMatch from pattern ${index + 1}")
                    }
                }
            }

            if (candidates.isNotEmpty()) {
                // For low priority, prefer standard OTP lengths (4-6 digits)
                val best = candidates
                    .filter { it.value.length in 4..8 }
                    .maxByOrNull { 
                        // Prefer 4-6 digit codes
                        val lengthScore = if (it.value.length in 4..6) 10 else 0
                        it.priority + lengthScore
                    }
                
                if (best != null) {
                    Log.d(TAG, "Returning OTP: ${best.value} (${best.source})")
                    return best.value
                }
            }
        }

        Log.d(TAG, "No OTP found in text")
        return null
    }

    /**
     * Cleans the matched OTP (removes spaces, dashes if it's all numeric)
     */
    private fun cleanMatch(match: String): String {
        val trimmed = match.trim()
        
        // If it contains only digits, spaces, and dashes, remove spaces and dashes
        return if (trimmed.matches(Regex("[0-9\\s-]+"))) {
            trimmed.replace("[-\\s]".toRegex(), "")
        } else {
            // For alphanumeric, keep as is but trim
            trimmed
        }
    }

    /**
     * Checks if the value is a valid OTP length
     */
    private fun isValidOtpLength(value: String): Boolean {
        return value.length in 3..12
    }

    /**
     * Determines if a value should be ignored
     * Simplified - rely on pattern specificity instead of over-filtering
     */
    private fun shouldIgnoreValue(value: String): Boolean {
        // Check against ignore patterns
        for (ignorePattern in ignorePatterns) {
            if (ignorePattern.matcher(value).matches()) {
                Log.d(TAG, "Ignoring value: $value (matches ignore pattern)")
                return true
            }
        }

        // Basic validation only
        when {
            // Too short or too long
            value.length < 3 || value.length > 12 -> {
                Log.d(TAG, "Ignoring value: $value (invalid length)")
                return true
            }
            
            // All same character (e.g., "111111", "AAAAAA")
            value.matches(Regex("(.)\\1+")) && value.length > 4 -> {
                Log.d(TAG, "Ignoring value: $value (repeating characters)")
                return true
            }
            
            else -> return false
        }
    }

    /**
     * Extracts all potential OTPs from text (for debugging/testing)
     */
    fun extractAllPotentialOtps(text: String): List<String> {
        val results = mutableListOf<String>()
        val allPatterns = highPriorityPatterns + mediumPriorityPatterns + lowPriorityPatterns
        
        for (pattern in allPatterns) {
            val matcher = pattern.matcher(text)
            while (matcher.find()) {
                val match = if (matcher.groupCount() > 0) {
                    matcher.group(1)
                } else {
                    matcher.group(0)
                }
                match?.let { 
                    val cleaned = cleanMatch(it)
                    if (!shouldIgnoreValue(cleaned)) {
                        results.add(cleaned)
                    }
                }
            }
        }
        return results.distinct()
    }

    /**
     * Test helper function to validate OTP extraction logic
     */
    fun testExtraction(testCases: List<Pair<String, String>>): List<Triple<String, String, Boolean>> {
        return testCases.map { (input, expected) ->
            val extracted = extractOtp(input)
            Triple(input, extracted ?: "null", extracted == expected)
        }
    }
}
