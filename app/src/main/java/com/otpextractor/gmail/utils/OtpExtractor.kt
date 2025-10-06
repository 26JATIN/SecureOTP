package com.otpextractor.gmail.utils

import android.util.Log
import java.util.regex.Pattern

object OtpExtractor {

    private const val TAG = "OtpExtractor"

    // Common OTP patterns
    private val otpPatterns = listOf(
        // Standard patterns for OTP/code/verification
        Pattern.compile("(?:otp|code|verification|pin|password)\\s*(?:is|:)?\\s*([0-9]{4,8})\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\b([0-9]{4,8})\\s*(?:is|as)\\s*(?:your|the)?\\s*(?:otp|code|verification|pin)\\b", Pattern.CASE_INSENSITIVE),
        
        // Patterns with phone numbers (like your example: "otp for 2310990533 is 4279")
        Pattern.compile("(?:otp|code|verification|pin|password)\\s*(?:for|to)?\\s*[0-9]{10,12}\\s*(?:is|:)?\\s*([0-9]{4,8})\\b", Pattern.CASE_INSENSITIVE),
        
        // Standalone numeric codes with word boundaries (4-8 digits)
        Pattern.compile("\\b([0-9]{4,8})\\b"),
        
        // Pattern for codes in brackets or quotes
        Pattern.compile("[\"'\\[\\(]([0-9]{4,8})[\"'\\]\\)]"),
        
        // Pattern for codes with dashes or spaces
        Pattern.compile("\\b([0-9]{2,4}[-\\s][0-9]{2,4})\\b"),
    )

    // Words that indicate an OTP context (for smart detection)
    private val otpKeywords = listOf(
        "otp", "verification", "code", "pin", "password", "authenticate",
        "verify", "security", "login", "signin", "confirm", "activation"
    )

    // Numbers to ignore (like phone numbers without OTP context)
    private val ignorePatterns = listOf(
        Pattern.compile("\\b[0-9]{10}\\b"), // 10-digit phone numbers alone
        Pattern.compile("\\b[0-9]{12}\\b"), // 12-digit numbers (likely phone or tracking)
        Pattern.compile("\\b[0-9]{13,}\\b"), // Very long numbers (tracking, dates, etc)
    )

    /**
     * Intelligently extracts OTP from text
     * Returns the most likely OTP code or null if none found
     */
    fun extractOtp(text: String): String? {
        if (text.isBlank()) return null

        Log.d(TAG, "Extracting OTP from: $text")

        val lowerText = text.lowercase()
        val hasOtpContext = otpKeywords.any { lowerText.contains(it) }

        // Try each pattern in order of priority
        for ((index, pattern) in otpPatterns.withIndex()) {
            val matcher = pattern.matcher(text)
            val matches = mutableListOf<String>()

            while (matcher.find()) {
                val match = if (matcher.groupCount() > 0) {
                    matcher.group(1) ?: continue
                } else {
                    matcher.group(0) ?: continue
                }

                // Clean the match (remove dashes/spaces)
                val cleanMatch = match.replace("[-\\s]".toRegex(), "")

                // Skip if it's a number we should ignore
                if (shouldIgnore(cleanMatch, hasOtpContext, index)) {
                    continue
                }

                matches.add(cleanMatch)
            }

            if (matches.isNotEmpty()) {
                // If we found matches with OTP context (first 3 patterns), return the first one
                if (index < 3) {
                    Log.d(TAG, "Found OTP with context: ${matches.first()}")
                    return matches.first()
                }

                // For standalone numbers, prefer those with OTP context nearby
                if (hasOtpContext) {
                    // Return the first number that's 4-6 digits (most common OTP length)
                    val preferredMatch = matches.firstOrNull { it.length in 4..6 }
                    if (preferredMatch != null) {
                        Log.d(TAG, "Found OTP from context: $preferredMatch")
                        return preferredMatch
                    }
                }

                // If still no good match and we're at standalone numbers, continue to next pattern
                if (index >= 3) continue

                // Return first match as fallback
                Log.d(TAG, "Found OTP (fallback): ${matches.first()}")
                return matches.first()
            }
        }

        Log.d(TAG, "No OTP found in text")
        return null
    }

    /**
     * Determines if a number should be ignored based on context
     */
    private fun shouldIgnore(number: String, hasOtpContext: Boolean, patternIndex: Int): Boolean {
        // If we have OTP context, be less strict
        if (hasOtpContext && patternIndex < 3) {
            return false
        }

        // Check ignore patterns
        for (ignorePattern in ignorePatterns) {
            if (ignorePattern.matcher(number).matches()) {
                return true
            }
        }

        // For standalone numbers without context, only accept 4-6 digits
        if (patternIndex >= 3 && !hasOtpContext) {
            return number.length !in 4..6
        }

        return false
    }

    /**
     * Extracts all potential OTPs from text (for debugging/testing)
     */
    fun extractAllPotentialOtps(text: String): List<String> {
        val results = mutableListOf<String>()
        for (pattern in otpPatterns) {
            val matcher = pattern.matcher(text)
            while (matcher.find()) {
                val match = if (matcher.groupCount() > 0) {
                    matcher.group(1)
                } else {
                    matcher.group(0)
                }
                match?.let { results.add(it.replace("[-\\s]".toRegex(), "")) }
            }
        }
        return results.distinct()
    }
}
