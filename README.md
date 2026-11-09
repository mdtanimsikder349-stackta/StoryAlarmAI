# Story Alarm AI

Android starter project for a personal alarm app.

## Current features
- Save one Instagram username locally.
- Choose an audio file from the phone.
- Test an alarm and stop it.
- Show a test notification.

## Important limitation
This starter does **not** automatically detect Instagram Stories. Instagram's official APIs do not generally provide arbitrary third-party public-account Story feeds. Do not enter Instagram passwords or upload session cookies to unofficial services.

## Build APK with GitHub Actions
1. Upload all files and folders in this project to the root of your GitHub repository.
2. Ensure the workflow file `.github/workflows/android.yml` is included.
3. Commit the files to the `main` branch.
4. Open the repository's **Actions** tab.
5. Open **Build Android APK** and wait for the run to finish.
6. Download the `StoryAlarmAI-debug-apk` artifact from the successful run.
7. Extract the downloaded artifact ZIP; install `app-debug.apk` on your Android phone.

The workflow builds a debug APK. It is suitable for personal testing, not Play Store release.
