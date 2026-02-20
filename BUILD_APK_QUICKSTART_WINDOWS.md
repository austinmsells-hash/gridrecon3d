# Build APK on Windows (Quickstart)

You do NOT need to be a programmer. Follow this checklist.

## Install tools
1) Install **Android Studio** (includes SDK + emulator tools).
2) During install, ensure these are checked:
   - Android SDK
   - Android SDK Platform-Tools
   - Android SDK Build-Tools
   - Android Emulator (optional)

## Open project
1) Unzip the GridRecon3D zip to a folder like:
   C:\Projects\GridRecon3D
2) Open Android Studio → **Open** → select:
   `android/`

Let it sync dependencies (first time can take a while).

## Build debug APK (fast)
Android Studio:
- Menu: **Build → Build Bundle(s) / APK(s) → Build APK(s)**

When finished:
- Click **Locate** in the notification.
- APK is usually here:
  `android/app/build/outputs/apk/debug/app-debug.apk`

## Install to your phone
1) Enable Developer Options:
   Settings → About Phone → tap “Build number” 7 times.
2) Enable USB debugging:
   Settings → Developer options → USB debugging.
3) Connect phone via USB.
4) In Android Studio:
   Run → “Select device” → choose your phone → Run.

OR install APK manually:
- Copy APK to phone, open it, and allow “Install unknown apps”.

## If build fails
- In Android Studio: File → **Invalidate Caches / Restart**
- Ensure SDK is installed: Settings → Android SDK
- Ensure Gradle sync completes with no errors
