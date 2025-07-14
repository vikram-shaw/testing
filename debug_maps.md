# 🔍 Google Maps API Key Debug Guide

Since you've replaced the API key but it's still not working, let's find the exact issue.

## 🚨 Step 1: Check Android Studio Logcat (MOST IMPORTANT)

Run your app and check **Logcat** for Google Maps error messages:

**In Android Studio:**
1. Open **Logcat** tab
2. Filter by: `Google|Maps|API`
3. Look for error messages

**Common error messages and solutions:**

### ❌ "This API project is not authorized to use this API"
**Solution:** Enable "Maps SDK for Android" in Google Cloud Console
- Go to: APIs & Services → Library
- Search: "Maps SDK for Android"
- Click **ENABLE**

### ❌ "The provided API key is not valid for this application"
**Solution:** API key restrictions are blocking this app
- Package name mismatch
- SHA-1 fingerprint mismatch
- Wrong restrictions

### ❌ "API_NOT_FOUND" or "REQUEST_DENIED"
**Solution:** API key configuration issue

## 🔧 Step 2: Quick Test - Remove ALL Restrictions

**Fastest way to test if your API key works:**

1. Go to **Google Cloud Console**
2. Navigate to: **APIs & Services → Credentials**
3. Click on your **API key**
4. Under **Application restrictions**: Select **"None"**
5. Under **API restrictions**: Select **"Don't restrict key"**
6. **Save** the changes
7. **Test your app** immediately

**If this works:** Your API key is fine, but restrictions are wrong
**If this doesn't work:** Your API key or API enablement has issues

## 📱 Step 3: Check Your Package Name

Your app package name is: `com.example.rush`

**Verify this matches your restrictions:**
1. Check `app/build.gradle.kts` → `applicationId = "com.example.rush"`
2. In Google Cloud Console → API key restrictions → add `com.example.rush`

## 🔑 Step 4: Get SHA-1 Fingerprint (For Debug Testing)

**Run this command on your development machine:**
```bash
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```

**Look for:**
```
SHA1: AA:BB:CC:DD:EE:FF:00:11:22:33:44:55:66:77:88:99:AA:BB:CC:DD
```

**Add this SHA-1 to your Google Cloud Console API key restrictions**

## 🧪 Step 5: Built-in App Testing

Your app has debugging features! Look for these in the running app:

1. **Fallback map message**: Shows if Google Maps failed to load
2. **Debug logs**: Check Android Studio Logcat for specific errors
3. **Loading timeout**: If it shows fallback after 5 seconds, Google Maps failed

## 📋 Step 6: Verification Checklist

**Google Cloud Console:**
- [ ] Maps SDK for Android is **ENABLED**
- [ ] API key has **no restrictions** (for testing)
- [ ] Billing is enabled (if required)
- [ ] API key is not **suspended** or **expired**

**Android App:**
- [ ] API key replaced in `AndroidManifest.xml`
- [ ] App cleaned and rebuilt (`./gradlew clean build`)
- [ ] Testing on device/emulator with internet connection
- [ ] Location permission granted

## 🔍 Step 7: Advanced Debugging

**Check exact API key in app:**
Add this temporary code to see what API key the app is reading:

```kotlin
// Add to MainActivity.onCreate() temporarily
val appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
val apiKey = appInfo.metaData?.getString("com.google.android.geo.API_KEY")
Log.d("DEBUG_API_KEY", "API Key: ${apiKey?.take(10)}...")
```

**Check network connectivity:**
- Maps need internet connection
- Try on Wi-Fi and mobile data
- Check firewall/proxy settings

## ⚡ Quick Fix Commands

**Clean and rebuild:**
```bash
./gradlew clean
./gradlew build
```

**Check if API key is in manifest:**
```bash
grep -r "API_KEY" app/src/main/AndroidManifest.xml
```

## 🆘 Common Solutions

**Solution 1: Wrong API Key Type**
- Make sure you're using an **Android API key**, not a web API key
- Create a new API key specifically for Android if needed

**Solution 2: Billing Issues**
- Google Maps requires billing to be enabled
- Check Google Cloud Console → Billing

**Solution 3: Package Name Mismatch**
- Your working app might use a different package name
- Use the SAME package name, or update restrictions

**Solution 4: Cache Issues**
- Uninstall the app completely
- Clean project: `./gradlew clean`
- Rebuild and reinstall

## 🎯 Next Steps

1. **Remove ALL restrictions** from API key (test this first!)
2. **Check Logcat** for exact error messages
3. **Share the specific error** you see in Logcat
4. **Verify "Maps SDK for Android" is enabled**

---

**Most likely issue:** API key restrictions are blocking your app's package name or SHA-1 fingerprint.

**Quick test:** Remove all restrictions → if it works, gradually add them back!