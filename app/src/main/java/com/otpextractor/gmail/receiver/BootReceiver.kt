package com.otpextractor.gmail.receiver

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.util.Log
import com.otpextractor.gmail.service.OtpListener
import com.otpextractor.gmail.utils.PreferenceManager

/**
 * BroadcastReceiver to handle device boot and restart OTP service
 * Ensures the OTP listener service starts automatically after device reboot
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val action = intent.action
        Log.d(TAG, "BootReceiver received action: $action")

        when (action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_LOCKED_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                Log.d(TAG, "Device booted or app updated - checking OTP service")
                ensureServiceRunning(context)
            }
            Intent.ACTION_USER_PRESENT -> {
                Log.d(TAG, "User unlocked device - verifying OTP service")
                ensureServiceRunning(context)
            }
        }
    }

    /**
     * Ensures the OTP listener service is running if user has enabled it
     */
    private fun ensureServiceRunning(context: Context) {
        try {
            val prefManager = PreferenceManager(context)
            
            // Check if user has enabled the service
            if (!prefManager.isServiceEnabled()) {
                Log.d(TAG, "Service is disabled by user, not starting")
                return
            }

            // Check if notification listener permission is granted
            if (!isNotificationListenerEnabled(context)) {
                Log.d(TAG, "Notification listener permission not granted")
                return
            }

            // On Android 7.0+, we can request rebind
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                try {
                    val componentName = ComponentName(context, OtpListener::class.java)
                    NotificationListenerService.requestRebind(componentName)
                    Log.d(TAG, "Requested service rebind after boot")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to request rebind: ${e.message}")
                }
            }

            Log.d(TAG, "OTP service should now be running")
        } catch (e: Exception) {
            Log.e(TAG, "Error ensuring service is running: ${e.message}", e)
        }
    }

    /**
     * Check if notification listener permission is enabled
     */
    private fun isNotificationListenerEnabled(context: Context): Boolean {
        val cn = ComponentName(context, OtpListener::class.java)
        val flat = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        )
        return flat != null && flat.contains(cn.flattenToString())
    }

    companion object {
        private const val TAG = "BootReceiver"
    }
}
