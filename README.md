# Pool Aim Pro 🎱

Android app written in Kotlin for **manual pool aiming**.

## Features
- Manually drag the white start point and yellow goal point.
- Adjust aim length and angle with sliders.
- Floating overlay window with draggable header.
- Reset and close controls.
- No automatic ball detection or game automation.

## Open in Android Studio
1. Open Android Studio and choose **Open**.
2. Select this repository's root folder (the folder containing `settings.gradle.kts`).
3. Wait for Gradle sync, then press **Run**.
4. To build an APK: **Build → Build Bundle(s) / APK(s) → Build APK(s)**.

## Floating overlay
Tap **Start Overlay** and grant Android's “Display over other apps” permission. Return to the app and tap the button again if needed.

## Limitations
This is a manually controlled visual guide. It does not detect balls, calculate game physics, or guarantee wins. Overlay visibility depends on the Android device and game compatibility. Manual coordinates may need adjustment for the game table.
