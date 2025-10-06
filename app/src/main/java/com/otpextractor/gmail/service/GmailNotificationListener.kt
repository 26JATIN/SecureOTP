package com.otpextractor.gmail.service

import android.app.Notification
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.otpextractor.gmail.OtpExtractorApp
import com.otpextractor.gmail.R
import com.otpextractor.gmail.utils.OtpExtractor
import com.otpextractor.gmail.utils.PreferenceManager

class GmailNotificationListener : NotificationListenerService() {

    private lateinit var prefManager: PreferenceManager
    private val processedNotifications = mutableSetOf<String>()

    override fun onCreate() {
        super.onCreate()
        prefManager = PreferenceManager(this)
        Log.d(TAG, "Gmail Notification Listener Service started")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (!prefManager.isServiceEnabled()) {
            Log.d(TAG, "Service is disabled, ignoring notification")
            return
        }

        // Check if notification is from Gmail
        if (!isGmailNotification(sbn)) {
            return
        }

        // Avoid processing the same notification multiple times
        val notificationKey = "${sbn.packageName}:${sbn.id}:${sbn.postTime}"
        if (processedNotifications.contains(notificationKey)) {
            return
        }

        Log.d(TAG, "Gmail notification detected from package: ${sbn.packageName}")

        try {
            val notification = sbn.notification
            val extras = notification?.extras

            if (extras != null) {
                val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
                val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
                val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString() ?: ""
                val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() ?: ""
                val infoText = extras.getCharSequence(Notification.EXTRA_INFO_TEXT)?.toString() ?: ""

                // Combine all text fields
                val fullText = "$title $text $subText $bigText $infoText"

                Log.d(TAG, "Notification content: $fullText")

                // Extract OTP
                val otp = OtpExtractor.extractOtp(fullText)
                if (otp != null) {
                    Log.d(TAG, "OTP found: $otp")
                    processedNotifications.add(notificationKey)
                    copyToClipboard(otp)
                    showOtpNotification(otp, sbn.packageName)
                    
                    // Clean up old processed notifications (keep last 50)
                    if (processedNotifications.size > 50) {
                        val toRemove = processedNotifications.take(10)
                        processedNotifications.removeAll(toRemove.toSet())
                    }
                } else {
                    Log.d(TAG, "No OTP found in notification")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing notification: ${e.message}", e)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        // Optional: Clean up if needed
    }

    private fun isGmailNotification(sbn: StatusBarNotification): Boolean {
        // Support multiple Gmail package names
        val gmailPackages = listOf(
            "com.google.android.gm",           // Gmail
            "com.google.android.apps.inbox"    // Inbox (legacy)
        )
        
        val isGmail = gmailPackages.contains(sbn.packageName)
        
        if (isGmail) {
            Log.d(TAG, "Gmail notification from: ${sbn.packageName}")
        }
        
        return isGmail
    }

    private fun copyToClipboard(otp: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("OTP", otp)
        clipboard.setPrimaryClip(clip)
        Log.d(TAG, "OTP copied to clipboard: $otp")
    }

    private fun showOtpNotification(otp: String, sourcePackage: String) {
        try {
            val accountInfo = if (sourcePackage.contains("gm")) "Gmail" else "Email"
            
            val notification = NotificationCompat.Builder(this, OtpExtractorApp.NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("OTP Copied!")
                .setContentText("$otp copied from $accountInfo")
                .setStyle(NotificationCompat.BigTextStyle()
                    .bigText("OTP: $otp\nCopied to clipboard from $accountInfo"))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setTimeoutAfter(5000) // Auto dismiss after 5 seconds
                .build()

            val notificationId = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
            NotificationManagerCompat.from(this).notify(notificationId, notification)
        } catch (e: Exception) {
            Log.e(TAG, "Error showing notification: ${e.message}", e)
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "Notification Listener connected")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.d(TAG, "Notification Listener disconnected")
    }

    companion object {
        private const val TAG = "GmailNotifListener"
    }
}
