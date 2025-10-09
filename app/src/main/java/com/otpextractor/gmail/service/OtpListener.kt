package com.otpextractor.secureotp.service

import android.app.Notification
import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.otpextractor.secureotp.OtpExtractorApp
import com.otpextractor.secureotp.R
import com.otpextractor.secureotp.utils.OtpExtractor
import com.otpextractor.secureotp.utils.PreferenceManager
import kotlinx.coroutines.*

class OtpListener : NotificationListenerService() {

    private lateinit var prefManager: PreferenceManager
    private val processedNotifications = mutableSetOf<String>()
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onCreate() {
        super.onCreate()
        prefManager = PreferenceManager(this)
        
        // Initialize WakeLock for efficient battery usage
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "SecureOTP::NotificationProcessing"
        )
        
        Log.d(TAG, "SecureOTP Notification Listener Service started")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (!prefManager.isServiceEnabled()) {
            Log.d(TAG, "Service is disabled, ignoring notification")
            return
        }

        // Avoid processing the same notification multiple times
        val notificationKey = "${sbn.packageName}:${sbn.id}:${sbn.postTime}"
        if (processedNotifications.contains(notificationKey)) {
            return
        }

        // Filter out system and our own notifications
        if (shouldIgnorePackage(sbn.packageName)) {
            return
        }

        Log.d(TAG, "Processing notification from: ${sbn.packageName}")

        // Process notification in background with minimal battery impact
        serviceScope.launch {
            try {
                // Acquire wake lock for minimal time (max 3 seconds)
                wakeLock?.acquire(3000L)
                
                val notification = sbn.notification
                val extras = notification?.extras

                if (extras != null) {
                    val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
                    val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
                    val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString() ?: ""
                    val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() ?: ""
                    val infoText = extras.getCharSequence(Notification.EXTRA_INFO_TEXT)?.toString() ?: ""
                    val summaryText = extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT)?.toString() ?: ""

                    // Combine all text fields
                    val fullText = "$title $text $subText $bigText $infoText $summaryText"

                    Log.d(TAG, "Notification content from ${sbn.packageName}: $fullText")

                    // Extract OTP with context
                    val otp = OtpExtractor.extractOtp(fullText)
                    if (otp != null) {
                        Log.d(TAG, "OTP found: $otp from ${sbn.packageName}")
                        processedNotifications.add(notificationKey)
                        
                        withContext(Dispatchers.Main) {
                            copyToClipboard(otp)
                            val appName = getAppName(sbn.packageName)
                            showOtpNotification(otp, appName)
                        }
                        
                        // Clean up old processed notifications (keep last 100)
                        if (processedNotifications.size > 100) {
                            val toRemove = processedNotifications.take(20)
                            processedNotifications.removeAll(toRemove.toSet())
                        }
                    } else {
                        Log.d(TAG, "No OTP found in notification from ${sbn.packageName}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing notification: ${e.message}", e)
            } finally {
                // Always release wake lock
                if (wakeLock?.isHeld == true) {
                    wakeLock?.release()
                }
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        // Minimal cleanup - no heavy processing
    }

    /**
     * Battery-efficient package filtering
     * Only ignore system packages and our own app
     */
    private fun shouldIgnorePackage(packageName: String): Boolean {
        // Ignore our own app
        if (packageName == applicationContext.packageName) {
            return true
        }
        
        // Ignore common system packages that never contain OTPs
        val systemPackagesToIgnore = setOf(
            "android",
            "com.android.systemui",
            "com.android.providers.downloads",
            "com.google.android.gms",
            "com.google.android.apps.photos",
            "com.google.android.apps.maps",
            "com.android.vending" // Play Store
        )
        
        return systemPackagesToIgnore.contains(packageName)
    }

    /**
     * Get user-friendly app name from package name
     */
    private fun getAppName(packageName: String): String {
        return try {
            val pm = packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            // Fallback to recognizable names
            when {
                packageName.contains("sms") || packageName.contains("messaging") -> "SMS"
                packageName.contains("gmail") || packageName.contains("gm") -> "Gmail"
                packageName.contains("whatsapp") -> "WhatsApp"
                packageName.contains("bank") -> "Banking App"
                packageName.contains("paytm") -> "Paytm"
                packageName.contains("phonepe") -> "PhonePe"
                packageName.contains("gpay") -> "Google Pay"
                packageName.contains("instagram") -> "Instagram"
                packageName.contains("facebook") -> "Facebook"
                packageName.contains("twitter") -> "Twitter"
                else -> packageName.substringAfterLast(".").replaceFirstChar { it.uppercase() }
            }
        }
    }

    private fun copyToClipboard(otp: String) {
        try {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("OTP", otp)
            clipboard.setPrimaryClip(clip)
            Log.d(TAG, "OTP copied to clipboard: $otp")
        } catch (e: Exception) {
            Log.e(TAG, "Error copying to clipboard: ${e.message}", e)
        }
    }

    private fun showOtpNotification(otp: String, appName: String) {
        try {
            val notification = NotificationCompat.Builder(this, OtpExtractorApp.NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(getString(R.string.otp_copied))
                .setContentText("$otp - from $appName")
                .setStyle(NotificationCompat.BigTextStyle()
                    .bigText("OTP: $otp\n\nCopied to clipboard from $appName\nReady to paste!"))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setTimeoutAfter(6000) // Auto dismiss after 6 seconds
                .setColor(resources.getColor(R.color.accent, null))
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .build()

            val notificationId = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
            NotificationManagerCompat.from(this).notify(notificationId, notification)
        } catch (e: Exception) {
            Log.e(TAG, "Error showing notification: ${e.message}", e)
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "OtpListener connected - monitoring all apps")
        
        // Register boot receiver to restart service after reboot
        registerBootReceiver()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.d(TAG, "OtpListener disconnected")
        
        // Try to reconnect
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            requestRebind(android.content.ComponentName(this, OtpListener::class.java))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up coroutines and wake lock
        serviceScope.cancel()
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
        Log.d(TAG, "OtpListener destroyed")
    }

    /**
     * Register receiver to restart service after device boot
     */
    @Suppress("UNUSED_VARIABLE")
    private fun registerBootReceiver() {
        try {
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_BOOT_COMPLETED)
                addAction(Intent.ACTION_LOCKED_BOOT_COMPLETED)
                addAction(Intent.ACTION_USER_PRESENT)
            }
            // Note: Boot receiver should be registered in manifest, not here
            // This is just for documentation
        } catch (e: Exception) {
            Log.e(TAG, "Error registering boot receiver: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "OtpListener"
    }
}
