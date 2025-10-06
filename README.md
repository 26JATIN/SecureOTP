# SecureOTP - Universal OTP Extractor for Android

A smart, battery-efficient Android utility app built with Kotlin that automatically detects, extracts, and copies OTP codes from **ANY app notifications** - SMS, Email, Banking apps, Social media, and more - to your clipboard instantly.

## ✨ Features

### 🌐 Universal App Support
- **SMS Messages**: Auto-extracts OTPs from all messaging apps
- **Email**: Gmail, Outlook, Yahoo Mail, ProtonMail, and more
- **Banking Apps**: All major banks and fintech apps  
- **Social Media**: WhatsApp, Instagram, Facebook, Twitter, Telegram
- **Payment Apps**: PayPal, Paytm, PhonePe, Google Pay, and more
- **Any App**: Works with any app that sends OTP notifications!

### 🎯 Smart Detection
- Intelligently identifies OTP codes even when notifications contain multiple numbers
- Filters out phone numbers, dates, and tracking numbers
- Context-aware pattern matching for maximum accuracy
- Supports various OTP formats:
  - "OTP for 2310990533 is 4279" → Copies `4279`
  - "Your verification code: 123456"  
  - "Use code 5678 to login"
  - "Authentication code is 9012"
  - And many more patterns!

### 🔋 Battery Optimized
- Efficient background processing with minimal battery impact
- Smart wake lock usage (max 3 seconds per notification)
- Uses coroutines for non-blocking operations
- No polling, no API calls, no 24/7 background services
- Hardware accelerated for smooth performance

### 🔒 Privacy & Security
- **100% Local Processing**: All OTP extraction happens on your device
- **No Internet Required**: Works completely offline
- **No Data Collection**: Zero data sent to servers
- **Open Source**: Fully transparent code you can audit
- **No Permissions Abuse**: Only uses NotificationListenerService

### ⚡ Performance
- Instant OTP detection as notifications arrive
- Auto-copy to clipboard - ready to paste immediately
- Minimal memory footprint
- Optimized for low-end devices

## Requirements

- Android 8.0 (API 26) or higher
- No internet connection needed
- No account setup required
- Works with all apps that send notifications

## Setup Instructions

### 1. Build and Install

1. Open the project in Android Studio
2. Sync Gradle files
3. Connect your Android device or start an emulator
4. Click "Run" or use:
   ```bash
   ./gradlew installDebug
   ```

### 2. Using the App

1. **Launch SecureOTP**
2. **Grant Notification Access**: Tap "Grant Notification Access" button
   - You'll be taken to Settings
   - Find and enable "SecureOTP" in the notification access list
3. **Enable OTP Detection**: Toggle the switch to ON
4. **Done!** The app will now automatically:
   - Monitor all app notifications for OTPs
   - Extract the OTP code intelligently
   - Copy it to your clipboard
   - Show you a notification with the copied OTP

### 3. Testing

Send yourself a test OTP via:
- SMS
- Email (Gmail, Outlook, etc.)
- Banking app notification
- Any app that sends OTPs

The OTP will be automatically copied to your clipboard!

## How It Works

1. **Notification Monitoring**: Uses Android's `NotificationListenerService` to monitor notifications from all apps
2. **Smart Filtering**: Filters out system notifications and non-relevant apps
3. **Pattern Matching**: Uses advanced regex patterns to identify OTP codes
4. **Context Analysis**: Understands OTP-related keywords to distinguish OTPs from other numbers
5. **Battery Efficient**: Processes notifications asynchronously with minimal wake lock usage
6. **Auto-Copy**: Immediately copies the detected OTP to clipboard

## Supported OTP Patterns

The app recognizes various OTP formats including:
- Standard: "OTP is 123456"
- SMS style: "123456 is your verification code"
- Banking: "Use code 123456"
- Email: "Your authentication code: 123456"
- With phone numbers: "OTP for 9876543210 is 123456"
- Formatted: "Code: 12-34-56"
- And many more!

## Privacy & Permissions

### Permissions Used:
- **Notification Access**: To read notifications for OTP extraction
- **Post Notifications**: To show you confirmation when OTP is copied
- **Wake Lock**: For efficient background processing (minimal usage)

### Privacy Guarantee:
- ✅ All processing happens locally on your device
- ✅ No network access or internet permission
- ✅ No data collection or analytics
- ✅ No ads or trackers
- ✅ Open source - you can verify yourself!

## Battery Optimization

SecureOTP is designed for minimal battery impact:
- Uses event-driven architecture (no polling)
- Coroutines for non-blocking operations  
- Wake lock held for max 3 seconds per notification
- Efficient memory management
- No background services when not needed

## Architecture

Built with modern Android development practices:
- **Language**: Kotlin
- **Architecture**: Service-based with coroutines
- **UI**: Material Design 3 components
- **Background Processing**: NotificationListenerService + Coroutines
- **Minimum SDK**: 26 (Android 8.0)
- **Target SDK**: 34 (Android 14)

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is open source. Feel free to use and modify as needed.

## Disclaimer

This app is for personal use to improve your OTP entry experience. Always ensure you're using it on your personal device and following your organization's security policies.
