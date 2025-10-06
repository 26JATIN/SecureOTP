package com.otpextractor.secureotp.utils

import android.app.ActivityManager
import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.annotation.RequiresApi

/**
 * Utility class to check background restrictions for apps (especially Gmail)
 * and guide users to unrestrict them for faster OTP delivery.
 */
object BackgroundRestrictionChecker {

    private const val GMAIL_PACKAGE = "com.google.android.gm"
    private const val GMAIL_GO_PACKAGE = "com.google.android.gm.lite"

    /**
     * Data class representing app restriction status
     */
    data class RestrictionStatus(
        val isInstalled: Boolean = false,
        val isBatteryOptimized: Boolean = false,
        val isBackgroundRestricted: Boolean = false,
        val isDataSaverEnabled: Boolean = false,
        val isDataRestricted: Boolean = false,
        val standbyBucket: Int = -1,
        val standbyBucketName: String = "Unknown"
    )

    /**
     * Check if Gmail is installed on the device
     */
    fun isGmailInstalled(context: Context): Boolean {
        val hasRegular = isAppInstalled(context, GMAIL_PACKAGE)
        val hasGo = isAppInstalled(context, GMAIL_GO_PACKAGE)
        android.util.Log.d("BackgroundRestrictionChecker", "Gmail regular: $hasRegular, Gmail Go: $hasGo")
        return hasRegular || hasGo
    }

    /**
     * Get the Gmail package name (regular or Go)
     */
    fun getGmailPackage(context: Context): String? {
        val regularInstalled = isAppInstalled(context, GMAIL_PACKAGE)
        val goInstalled = isAppInstalled(context, GMAIL_GO_PACKAGE)
        android.util.Log.d("BackgroundRestrictionChecker", "Checking Gmail - Regular: $regularInstalled, Go: $goInstalled")
        return when {
            regularInstalled -> GMAIL_PACKAGE
            goInstalled -> GMAIL_GO_PACKAGE
            else -> null
        }
    }

    /**
     * Check comprehensive restriction status for Gmail
     */
    fun checkGmailRestrictions(context: Context): RestrictionStatus {
        val gmailPackage = getGmailPackage(context) ?: return RestrictionStatus()

        return RestrictionStatus(
            isInstalled = true,
            isBatteryOptimized = isBatteryOptimized(context, gmailPackage),
            isBackgroundRestricted = isBackgroundRestricted(context, gmailPackage),
            isDataSaverEnabled = isDataSaverEnabled(context),
            isDataRestricted = isDataRestricted(context, gmailPackage),
            standbyBucket = getStandbyBucket(context, gmailPackage),
            standbyBucketName = getStandbyBucketName(getStandbyBucket(context, gmailPackage))
        )
    }

    /**
     * Check if any restrictions are active for Gmail
     */
    fun hasGmailRestrictions(context: Context): Boolean {
        val status = checkGmailRestrictions(context)
        return status.isInstalled && (
            status.isBatteryOptimized ||
            status.isBackgroundRestricted ||
            status.isDataRestricted ||
            status.standbyBucket >= UsageStatsManager.STANDBY_BUCKET_RARE
        )
    }

    /**
     * Check if app is installed
     */
    private fun isAppInstalled(context: Context, packageName: String): Boolean {
        return try {
            val pm = context.packageManager
            @Suppress("DEPRECATION")
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getPackageInfo(packageName, android.content.pm.PackageManager.PackageInfoFlags.of(0))
            } else {
                pm.getPackageInfo(packageName, 0)
            }
            packageInfo != null
        } catch (e: android.content.pm.PackageManager.NameNotFoundException) {
            false
        } catch (e: Exception) {
            android.util.Log.e("BackgroundRestrictionChecker", "Error checking if $packageName is installed", e)
            false
        }
    }

    /**
     * Check if battery optimization is enabled for an app
     */
    fun isBatteryOptimized(context: Context, packageName: String): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return false
        
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return !powerManager.isIgnoringBatteryOptimizations(packageName)
    }

    /**
     * Check if background restrictions are enabled for an app
     */
    fun isBackgroundRestricted(context: Context, packageName: String): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return false
        
        return try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            activityManager.isBackgroundRestricted
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Check if Data Saver is enabled system-wide
     */
    fun isDataSaverEnabled(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return false
        
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.restrictBackgroundStatus == ConnectivityManager.RESTRICT_BACKGROUND_STATUS_ENABLED
    }

    /**
     * Check if data restrictions are applied to specific app
     */
    fun isDataRestricted(context: Context, packageName: String): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return false
        
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            // Check if app is restricted when data saver is on
            val restrictBackgroundStatus = connectivityManager.restrictBackgroundStatus
            restrictBackgroundStatus == ConnectivityManager.RESTRICT_BACKGROUND_STATUS_ENABLED
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get app standby bucket (Active, Working Set, Frequent, Rare, Restricted)
     */
    @RequiresApi(Build.VERSION_CODES.P)
    fun getStandbyBucket(context: Context, packageName: String): Int {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return -1
        
        return try {
            val usageStatsManager = context.getSystemService("usagestats") as? UsageStatsManager
            usageStatsManager?.getAppStandbyBucket() ?: -1
        } catch (e: Exception) {
            -1
        }
    }

    /**
     * Convert standby bucket to human-readable name
     */
    fun getStandbyBucketName(bucket: Int): String {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return "Not Available"
        
        return when (bucket) {
            UsageStatsManager.STANDBY_BUCKET_ACTIVE -> "Active"
            UsageStatsManager.STANDBY_BUCKET_WORKING_SET -> "Working Set"
            UsageStatsManager.STANDBY_BUCKET_FREQUENT -> "Frequent"
            UsageStatsManager.STANDBY_BUCKET_RARE -> "Rare (Delayed)"
            50 -> "Restricted (Very Delayed)" // STANDBY_BUCKET_RESTRICTED (API 30+)
            else -> "Unknown"
        }
    }

    /**
     * Open battery optimization settings directly for Gmail app
     * Opens Gmail's app info page where user can manage battery optimization
     */
    fun openBatteryOptimizationSettings(context: Context, packageName: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return
        
        try {
            // Try to open battery optimization directly for this specific app
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = android.net.Uri.parse("package:$packageName")
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback 1: Try app details page (has battery settings)
            try {
                openAppDetailsSettings(context, packageName)
            } catch (e2: Exception) {
                // Fallback 2: General battery optimization list
                try {
                    val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                    context.startActivity(intent)
                } catch (e3: Exception) {
                    android.util.Log.e("BackgroundRestrictionChecker", "Failed to open battery settings", e3)
                }
            }
        }
    }

    /**
     * Open app details/info page in system settings
     */
    fun openAppDetailsSettings(context: Context, packageName: String) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = android.net.Uri.parse("package:$packageName")
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Open Data Saver settings
     */
    fun openDataSaverSettings(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return
        
        try {
            val intent = Intent(Settings.ACTION_IGNORE_BACKGROUND_DATA_RESTRICTIONS_SETTINGS).apply {
                data = android.net.Uri.parse("package:${getGmailPackage(context)}")
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback: open data usage settings
            try {
                val intent = Intent(Settings.ACTION_DATA_USAGE_SETTINGS)
                context.startActivity(intent)
            } catch (e2: Exception) {
                e2.printStackTrace()
            }
        }
    }

    /**
     * Generate user-friendly restriction description
     */
    fun getRestrictionDescription(status: RestrictionStatus): String {
        if (!status.isInstalled) {
            return "Gmail is not installed on this device."
        }

        val issues = mutableListOf<String>()

        if (status.isBatteryOptimized) {
            issues.add("⚡ Battery optimization is limiting Gmail's background sync")
        }

        if (status.isBackgroundRestricted) {
            issues.add("🚫 Background activity is restricted for Gmail")
        }

        if (status.isDataSaverEnabled && status.isDataRestricted) {
            issues.add("📡 Data Saver is preventing Gmail from syncing in background")
        }

        if (status.standbyBucket >= UsageStatsManager.STANDBY_BUCKET_RARE) {
            issues.add("⏸️ Gmail is in '${status.standbyBucketName}' mode - notifications are delayed")
        }

        if (issues.isEmpty()) {
            return "✅ Gmail has no restrictions - notifications should arrive quickly!"
        }

        return issues.joinToString("\n\n")
    }

    /**
     * Get actionable recommendation for user
     */
    fun getRecommendation(status: RestrictionStatus): String {
        if (!status.isInstalled) return ""

        val recommendations = mutableListOf<String>()

        if (status.isBatteryOptimized) {
            recommendations.add("Disable battery optimization for Gmail")
        }

        if (status.isBackgroundRestricted) {
            recommendations.add("Allow Gmail to run in background")
        }

        if (status.isDataSaverEnabled && status.isDataRestricted) {
            recommendations.add("Disable Data Saver or whitelist Gmail")
        }

        if (status.standbyBucket >= UsageStatsManager.STANDBY_BUCKET_RARE) {
            recommendations.add("Open Gmail frequently to improve priority")
        }

        return if (recommendations.isEmpty()) {
            "Gmail is optimized for instant OTP delivery!"
        } else {
            "To get faster Gmail OTPs:\n\n• " + recommendations.joinToString("\n• ")
        }
    }
}
