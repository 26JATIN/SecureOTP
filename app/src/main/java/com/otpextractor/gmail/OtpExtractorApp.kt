package com.otpextractor.secureotp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class OtpExtractorApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "OTP Extractor Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Background service for monitoring Gmail OTPs"
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "otp_extractor_channel"
    }
}
