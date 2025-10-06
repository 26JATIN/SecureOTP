package com.otpextractor.secureotp

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.otpextractor.secureotp.databinding.ActivityGmailOptimizationBinding
import com.otpextractor.secureotp.utils.BackgroundRestrictionChecker

class GmailOptimizationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGmailOptimizationBinding
    private var gmailPackage: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGmailOptimizationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        checkGmailStatus()
    }

    override fun onResume() {
        super.onResume()
        // Refresh status when user returns from settings
        checkGmailStatus()
    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        binding.btnFixBattery.setOnClickListener {
            gmailPackage?.let { pkg ->
                BackgroundRestrictionChecker.openBatteryOptimizationSettings(this, pkg)
            }
        }

        binding.btnFixBackground.setOnClickListener {
            gmailPackage?.let { pkg ->
                BackgroundRestrictionChecker.openAppDetailsSettings(this, pkg)
            }
        }

        binding.btnFixDataSaver.setOnClickListener {
            BackgroundRestrictionChecker.openDataSaverSettings(this)
        }

        binding.btnOpenGmail.setOnClickListener {
            gmailPackage?.let { pkg ->
                try {
                    val intent = packageManager.getLaunchIntentForPackage(pkg)
                    if (intent != null) {
                        startActivity(intent)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        binding.btnRefresh.setOnClickListener {
            checkGmailStatus()
        }
    }

    private fun checkGmailStatus() {
        // Check if Gmail is installed
        if (!BackgroundRestrictionChecker.isGmailInstalled(this)) {
            showGmailNotInstalled()
            return
        }

        gmailPackage = BackgroundRestrictionChecker.getGmailPackage(this)

        // Get comprehensive restriction status
        val status = BackgroundRestrictionChecker.checkGmailRestrictions(this)

        // Update UI based on status
        updateStatusUI(status)
    }

    private fun showGmailNotInstalled() {
        binding.statusContainer.visibility = View.VISIBLE
        binding.issuesContainer.visibility = View.GONE
        binding.btnRefresh.visibility = View.GONE

        binding.tvStatusTitle.text = "Gmail Not Found"
        binding.tvStatusDescription.text = "Gmail is not installed on this device.\n\nThis optimization is specifically for Gmail OTP delivery. If you use other email apps, they should work fine with the notification listener."
        
        binding.iconStatus.setImageResource(R.drawable.ic_warning)
        binding.iconStatus.setColorFilter(
            ContextCompat.getColor(this, R.color.warning)
        )
    }

    private fun updateStatusUI(status: BackgroundRestrictionChecker.RestrictionStatus) {
        binding.statusContainer.visibility = View.VISIBLE
        binding.issuesContainer.visibility = View.VISIBLE
        binding.btnRefresh.visibility = View.VISIBLE

        val hasRestrictions = BackgroundRestrictionChecker.hasGmailRestrictions(this)

        if (hasRestrictions) {
            // Show issues
            binding.tvStatusTitle.text = "⚡ Gmail Sync May Be Delayed"
            binding.tvStatusDescription.text = BackgroundRestrictionChecker.getRestrictionDescription(status)
            
            binding.iconStatus.setImageResource(R.drawable.ic_warning)
            binding.iconStatus.setColorFilter(
                ContextCompat.getColor(this, R.color.warning)
            )

            // Show relevant fix buttons
            setupFixButtons(status)
            
            binding.tvRecommendation.text = BackgroundRestrictionChecker.getRecommendation(status)
        } else {
            // All good!
            binding.tvStatusTitle.text = "✅ Gmail is Optimized!"
            binding.tvStatusDescription.text = "Gmail has no restrictions and should deliver OTP notifications instantly.\n\nIf you still experience delays, try opening Gmail frequently to improve its priority."
            
            binding.iconStatus.setImageResource(R.drawable.ic_check_circle)
            binding.iconStatus.setColorFilter(
                ContextCompat.getColor(this, R.color.success)
            )

            // Hide all fix buttons
            binding.fixButtonsContainer.visibility = View.GONE
            binding.tvRecommendation.visibility = View.GONE
        }

        // Update detailed info
        updateDetailedInfo(status)
    }

    private fun setupFixButtons(status: BackgroundRestrictionChecker.RestrictionStatus) {
        binding.fixButtonsContainer.visibility = View.VISIBLE
        binding.tvRecommendation.visibility = View.VISIBLE

        // Battery optimization
        if (status.isBatteryOptimized) {
            binding.cardBattery.visibility = View.VISIBLE
        } else {
            binding.cardBattery.visibility = View.GONE
        }

        // Background restrictions
        if (status.isBackgroundRestricted) {
            binding.cardBackground.visibility = View.VISIBLE
        } else {
            binding.cardBackground.visibility = View.GONE
        }

        // Data Saver
        if (status.isDataSaverEnabled && status.isDataRestricted) {
            binding.cardDataSaver.visibility = View.VISIBLE
        } else {
            binding.cardDataSaver.visibility = View.GONE
        }

        // Standby bucket (open Gmail to improve)
        if (status.standbyBucket >= android.app.usage.UsageStatsManager.STANDBY_BUCKET_RARE) {
            binding.cardStandby.visibility = View.VISIBLE
            binding.tvStandbyHelp.text = "Gmail is in '${status.standbyBucketName}' mode. Open Gmail regularly to improve priority."
        } else {
            binding.cardStandby.visibility = View.GONE
        }
    }

    private fun updateDetailedInfo(status: BackgroundRestrictionChecker.RestrictionStatus) {
        val details = buildString {
            append("📊 Detailed Status:\n\n")
            
            append("Battery Optimization: ")
            append(if (status.isBatteryOptimized) "❌ Enabled (delays sync)" else "✅ Disabled")
            append("\n\n")
            
            append("Background Activity: ")
            append(if (status.isBackgroundRestricted) "❌ Restricted" else "✅ Allowed")
            append("\n\n")
            
            append("Data Saver: ")
            append(if (status.isDataSaverEnabled) "❌ Enabled (blocks background data)" else "✅ Disabled")
            append("\n\n")
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                append("App Standby Bucket: ")
                append(status.standbyBucketName)
                append("\n")
                when (status.standbyBucket) {
                    android.app.usage.UsageStatsManager.STANDBY_BUCKET_ACTIVE -> 
                        append("✅ Best performance")
                    android.app.usage.UsageStatsManager.STANDBY_BUCKET_WORKING_SET -> 
                        append("✅ Good performance")
                    android.app.usage.UsageStatsManager.STANDBY_BUCKET_FREQUENT -> 
                        append("⚠️ Moderate delays")
                    android.app.usage.UsageStatsManager.STANDBY_BUCKET_RARE -> 
                        append("❌ Significant delays")
                    50 -> append("❌ Severe delays") // RESTRICTED
                }
            }
        }

        binding.tvDetailedInfo.text = details
    }
}
