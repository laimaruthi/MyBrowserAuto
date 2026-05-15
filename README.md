# MyBrowserAuto

A minimal Android WebView browser app, built in the cloud via GitHub Actions — no Android Studio or local SDK required.

## How to get the APK

1. Create a new repo on GitHub (e.g. `MyBrowserAuto`).
2. From this folder, push the code:
   ```bash
   git init
   git add .
   git commit -m "Initial commit"
   git branch -M main
   git remote add origin https://github.com/<your-username>/MyBrowserAuto.git
   git push -u origin main
   ```
3. Go to the repo on GitHub → **Actions** tab → wait for **Build Android APK** to finish (~3–5 min).
4. Click the run → scroll to **Artifacts** → download `MyBrowserAuto-debug-apk.zip`.
5. Unzip → transfer the `.apk` to your Android phone (USB / Google Drive / email).
6. On the phone, open the APK and tap **Install**. You may need to enable
   *Settings → Apps → Special access → Install unknown apps* for your file manager.

## Manually trigger a build
GitHub → **Actions** → **Build Android APK** → **Run workflow**.

## Project layout
```
app/
  src/main/
    AndroidManifest.xml
    java/com/mybrowser/auto/MainActivity.java
    res/layout/activity_main.xml
    res/values/strings.xml, themes.xml
  build.gradle
build.gradle
settings.gradle
.github/workflows/android.yml
```
