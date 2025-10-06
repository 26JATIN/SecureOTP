package com.otpextractor.secureotp.utils

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun setServiceEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SERVICE_ENABLED, enabled).apply()
    }

    fun isServiceEnabled(): Boolean {
        return prefs.getBoolean(KEY_SERVICE_ENABLED, true) // Default enabled
    }

    fun incrementOtpCount() {
        val count = getOtpCount()
        prefs.edit().putInt(KEY_OTP_COUNT, count + 1).apply()
    }

    fun getOtpCount(): Int {
        return prefs.getInt(KEY_OTP_COUNT, 0)
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREF_NAME = "otp_extractor_prefs"
        private const val KEY_SERVICE_ENABLED = "service_enabled"
        private const val KEY_OTP_COUNT = "otp_count"
    }
}
