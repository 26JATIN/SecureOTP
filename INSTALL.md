# Installation Guide

## ✅ Build Successful!

Your OTP Extractor APK has been built successfully!

### 📱 APK Location

```
/home/jatin/Documents/otp extractor/app/build/outputs/apk/debug/app-debug.apk
```

## Installation Options

### Option 1: Install via ADB (USB)

1. **Enable Developer Options** on your Android device:
   - Go to Settings → About Phone
   - Tap "Build Number" 7 times
   - Go back to Settings → Developer Options
   - Enable "USB Debugging"

2. **Connect your device** via USB

3. **Install the APK**:
   ```bash
   adb install "/home/jatin/Documents/otp extractor/app/build/outputs/apk/debug/app-debug.apk"
   ```

### Option 2: Install via File Transfer

1. **Copy APK to your phone**:
   ```bash
   # Using ADB
   adb push "/home/jatin/Documents/otp extractor/app/build/outputs/apk/debug/app-debug.apk" /sdcard/Download/

   # Or transfer via USB cable, Bluetooth, email, etc.
   ```

2. **Install on phone**:
   - Open "Files" or "Downloads" app on your phone
   - Tap on `app-debug.apk`
   - Allow "Install from Unknown Sources" if prompted
   - Tap "Install"

### Option 3: Wireless ADB (if device and PC are on same network)

1. **Enable wireless debugging** (Android 11+):
   - Settings → Developer Options → Wireless Debugging

2. **Connect**:
   ```bash
   adb pair <IP>:<PORT>  # Use pairing code shown on phone
   adb connect <IP>:<PORT>
   adb install "/home/jatin/Documents/otp extractor/app/build/outputs/apk/debug/app-debug.apk"
   ```

## 🚀 Using the App

Once installed:

1. **Open "OTP Extractor"** app
2. **Tap "Grant Notification Access"** button
3. In Settings, **enable "OTP Extractor"**
4. Return to app and **toggle the switch ON**
5. **Done!** The app is now monitoring Gmail notifications

## 📧 Testing

To test the app:

1. Send yourself a test email with OTP format:
   - Example: "Your OTP is 123456"
   - Or: "OTP for 9876543210 is 4279"

2. When Gmail notification appears:
   - OTP will be automatically copied to clipboard
   - You'll see a confirmation notification

3. Paste anywhere to verify!

## 🔧 Troubleshooting

### App won't install
- Enable "Install from Unknown Sources" in Settings
- Or Settings → Apps → Special Access → Install Unknown Apps → Enable for your file manager

### Notification access not working
- Go to: Settings → Apps → Special Access → Notification Access
- Ensure "OTP Extractor" is enabled

### OTP not detected
- Make sure the switch is toggled ON in the app
- Check that Gmail notifications are enabled
- View logs: `adb logcat | grep GmailNotif`

## 📊 Build Info

- **Package**: com.otpextractor.gmail
- **Version**: 1.0 (versionCode 1)
- **Min SDK**: Android 8.0 (API 26)
- **Target SDK**: Android 14 (API 34)
- **Build Type**: Debug (signed with debug keystore)

## 🔄 Rebuilding

To rebuild after making changes:

```bash
cd "/home/jatin/Documents/otp extractor"
./gradlew clean assembleDebug
```

The new APK will be in the same location.

## 📝 Notes

- This is a **debug build** - for production, use `assembleRelease`
- The app works **completely offline**
- Supports **multiple Gmail accounts** automatically
- No internet permission required
- All processing happens locally on device

Enjoy your smart OTP extractor! 🎉
