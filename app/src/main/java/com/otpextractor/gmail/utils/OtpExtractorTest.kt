package com.otpextractor.gmail.utils

/**
 * Test cases for OTP Extractor
 * Run this to verify OTP extraction logic
 */
object OtpExtractorTest {

    fun runTests(): String {
        val testCases = listOf(
            // Critical test cases from user pattern analysis
            "please use OTP-890227 to accept delivery" to "890227",
            "bank send otp 123456" to "123456",
            "6767 is otp for 3838" to "6767",
            "otp of 4554 is 9988" to "9988",
            "2322 is otp for 2284" to "2322",
            "2310990533 is otp for 6767" to "2310990533",
            
            // Standard patterns - OTP after "is"
            "Your OTP is 123456" to "123456",
            "OTP: 4567" to "4567",
            "code is 789012" to "789012",
            "Your verification code is 789012" to "789012",
            "Use code 5432 to login" to "5432",
            "123456 is your verification code" to "123456",
            
            // "Use" keyword patterns
            "Use 456789 to verify" to "456789",
            "Please use 567890 to complete" to "567890",
            "Enter 678901 to proceed" to "678901",
            "Type 789012 to confirm" to "789012",
            "use OTP 890123" to "890123",
            
            // With phone numbers
            "OTP for 9876543210 is 4279" to "4279",
            "Your OTP for +919876543210 is 567890" to "567890",
            
            // Banking/Fintech patterns
            "Your authentication code is 445566" to "445566",
            "Authorization code: 778899" to "778899",
            "Enter code 3344 to proceed" to "3344",
            
            // SMS style
            "654321 is your OTP for login" to "654321",
            "987654 - Your verification code" to "987654",
            
            // Formatted codes
            "Your OTP is [123456]" to "123456",
            "Code: \"4567\"" to "4567",
            "OTP: <8899>" to "8899",
            
            // With dashes/spaces
            "Your code is 12-34-56" to "123456",
            "OTP: 98 76 54" to "987654",
            
            // Real-world examples
            "Hi, 567890 is your OTP for PhonePe login" to "567890",
            "Your Amazon OTP is 445566. Do not share" to "445566",
            "Google verification code: 889900" to "889900",
            "WhatsApp code: 665544" to "665544",
            "Your Instagram code is 123890" to "123890",
            
            // Edge cases - should NOT extract phone numbers
            "Call me at 9876543210" to "",
            "Order number 123456789012" to "",
        )

        val results = testCases.map { (input, expected) ->
            val extracted = OtpExtractor.extractOtp(input) ?: ""
            val expectedValue = expected.ifEmpty { null }
            val success = if (expectedValue == null) {
                extracted.isEmpty()
            } else {
                extracted == expectedValue
            }
            Triple(input, extracted, success)
        }
        
        val output = StringBuilder()
        output.appendLine("=== OTP Extractor Test Results ===\n")
        
        var passed = 0
        var failed = 0
        
        results.forEach { (input, extracted, success) ->
            val expectedVal = testCases.find { it.first == input }?.second ?: ""
            if (success) {
                passed++
                output.appendLine("✅ PASS: \"$input\" -> $extracted")
            } else {
                failed++
                output.appendLine("❌ FAIL: \"$input\" -> $extracted (expected: $expectedVal)")
            }
        }
        
        output.appendLine("\n=== Summary ===")
        output.appendLine("Total: ${results.size}")
        output.appendLine("Passed: $passed")
        output.appendLine("Failed: $failed")
        output.appendLine("Success Rate: ${(passed * 100.0 / results.size).toInt()}%")
        
        return output.toString()
    }
}
