package com.otpextractor.gmail

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.otpextractor.gmail.databinding.ActivityMainBinding
import com.otpextractor.gmail.service.OtpListener
import com.otpextractor.gmail.utils.PreferenceManager

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefManager: PreferenceManager

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefManager = PreferenceManager(this)

        setupUI()
        checkPermissions()
        updateUI()
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    private fun setupUI() {
        binding.btnEnableService.setOnClickListener {
            openNotificationListenerSettings()
        }

        binding.switchService.setOnCheckedChangeListener { _, isChecked ->
            prefManager.setServiceEnabled(isChecked)
            if (isChecked && !isNotificationListenerEnabled()) {
                showEnableListenerDialog()
                binding.switchService.isChecked = false
            } else {
                updateStatusText()
            }
        }
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun updateUI() {
        val isListenerEnabled = isNotificationListenerEnabled()
        val isServiceEnabled = prefManager.isServiceEnabled()

        binding.switchService.isEnabled = isListenerEnabled
        binding.switchService.isChecked = isServiceEnabled

        if (isListenerEnabled) {
            binding.tvStatus.text = getString(R.string.access_granted)
            binding.btnEnableService.isEnabled = false
            binding.btnEnableService.alpha = 0.5f
        } else {
            binding.tvStatus.text = getString(R.string.access_required)
            binding.btnEnableService.isEnabled = true
            binding.btnEnableService.alpha = 1.0f
        }

        updateStatusText()
    }

    private fun updateStatusText() {
        val isListenerEnabled = isNotificationListenerEnabled()
        val isServiceEnabled = prefManager.isServiceEnabled()

        binding.tvServiceStatus.text = when {
            !isListenerEnabled -> getString(R.string.service_inactive)
            !isServiceEnabled -> getString(R.string.service_disabled)
            else -> getString(R.string.service_active)
        }
    }

    private fun isNotificationListenerEnabled(): Boolean {
        val cn = ComponentName(this, OtpListener::class.java)
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        return flat != null && flat.contains(cn.flattenToString())
    }

    private fun openNotificationListenerSettings() {
        try {
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            startActivity(intent)
            Toast.makeText(
                this,
                "Please enable 'SecureOTP' in the list",
                Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Unable to open settings", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showEnableListenerDialog() {
        AlertDialog.Builder(this)
            .setTitle("Notification Access Required")
            .setMessage("To automatically detect and copy OTPs from any app (SMS, Email, Banking, etc.), SecureOTP needs notification access.\n\nYour privacy is protected - all processing happens locally on your device.\n\nPlease enable 'SecureOTP' in the next screen.")
            .setPositiveButton("Open Settings") { _, _ ->
                openNotificationListenerSettings()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
