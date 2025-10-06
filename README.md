# Gmail OTP Extractor for Android

A smart Android utility app built with Kotlin that automatically detects, extracts, and copies OTP codes from Gmail **notifications** to your clipboard instantly.

## Features

✨ **Smart OTP Detection**: Intelligently identifies OTP codes even when notifications contain multiple numbers (e.g., phone numbers)

� **Works Offline**: No internet required - monitors Gmail notifications locally

🔋 **Battery Efficient**: Uses NotificationListenerService - no polling or API calls

📧 **Multi-Account Support**: Works with all Gmail accounts on your device automatically

📋 **Auto-Copy**: Automatically copies detected OTPs to your clipboard

🔒 **Privacy-Focused**: All processing happens locally on your device

⚡ **Instant Detection**: Extracts OTP as soon as notification arrives

🎯 **Context-Aware**: Understands various OTP formats:
- "OTP for 2310990533 is 4279" → Copies `4279`
- "Your verification code: 123456"
- "Password is 5678"
- And many more patterns

## Requirements

- Android 8.0 (API 26) or higher
- Gmail app installed
- No internet connection needed
- No Google account setup required

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

1. **Launch the app**
2. **Grant Notification Access**: Tap "Grant Notification Access" button
   - You'll be taken to Settings
   - Find and enable "OTP Extractor"
3. **Enable Monitoring**: Toggle the switch ON
4. **Done!**: The app will now monitor Gmail notifications

When a Gmail notification with OTP arrives, the app will:
- Detect the OTP code instantly
- Copy it to your clipboard
- Show a notification confirming the copy

## How It Works

The app uses `NotificationListenerService` to monitor Gmail notifications in real-time and intelligent techniques to extract OTPs:

1. **Pattern Matching**: Uses regex patterns to identify common OTP formats
2. **Context Analysis**: Looks for keywords like "OTP", "verification", "code"
3. **Smart Filtering**: Ignores phone numbers and other non-OTP numeric values
4. **Priority System**: Prioritizes codes found near OTP-related keywords

### Supported OTP Patterns

- `OTP is 1234`
- `Your code: 5678`
- `Verification code 9012`
- `OTP for 9876543210 is 4279` (ignores phone number, extracts OTP)
- Codes in brackets: `[1234]`
- And many more variations

## Project Structure

```
app/
├── src/main/
│   ├── java/com/otpextractor/gmail/
│   │   ├── MainActivity.kt                    # Main UI
│   │   ├── OtpExtractorApp.kt                # Application class
│   │   ├── service/
│   │   │   └── GmailNotificationListener.kt  # Notification monitoring service
│   │   └── utils/
│   │       ├── OtpExtractor.kt               # Smart OTP extraction logic
│   │       └── PreferenceManager.kt          # Shared preferences
│   └── res/
│       ├── layout/
│       │   └── activity_main.xml        # UI layout
│       └── values/
│           ├── strings.xml
│           ├── colors.xml
│           └── themes.xml
```

## Permissions

The app requires the following permissions:

- **Notification Access**: To read Gmail notifications (granted through Settings)
- `POST_NOTIFICATIONS`: To show OTP copy confirmations

## Customization

### Add Custom OTP Patterns

Edit `OtpExtractor.kt` and add patterns to the `otpPatterns` list:

```kotlin
Pattern.compile("your_pattern_here", Pattern.CASE_INSENSITIVE)
```

## Troubleshooting

### OTPs Not Being Detected

1. **Check Notification Access**: 
   - Go to Settings → Apps → Special access → Notification access
   - Ensure "OTP Extractor" is enabled
2. **Verify Gmail notifications are enabled**: Check Gmail app notification settings
3. **Toggle the switch**: Make sure OTP Monitoring is ON in the app
4. **Check logcat for errors**: `adb logcat | grep GmailNotif`

### Notification Access Gets Disabled

Some Android devices may revoke notification access:
- Re-enable it from the app or Settings
- Check if battery optimization is affecting the app
- Some manufacturers (Xiaomi, Oppo) have aggressive battery management - whitelist the app

### Multiple Accounts

The app automatically works with all Gmail accounts on your device. No additional setup needed!

## Privacy & Security

- The app only reads Gmail notification content locally
- **No internet access required** - completely offline
- No data is stored or transmitted to external servers
- All processing happens locally on your device
- No account credentials needed or accessed

## Future Enhancements

- [ ] Support for other apps (SMS, WhatsApp, Telegram, etc.)
- [ ] Custom OTP pattern configuration UI
- [ ] OTP history log
- [ ] Statistics (OTPs copied today/week/month)
- [ ] Widget support
- [ ] Dark theme
- [ ] Whitelist/blacklist specific email senders

## License

This project is provided as-is for educational and personal use.

## Contributing

Feel free to submit issues, fork the repository, and create pull requests for any improvements.

## Developer

Built with ❤️ using Kotlin and Android best practices.
