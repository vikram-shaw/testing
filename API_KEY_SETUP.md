# Google Maps API Key Setup Guide

## 🔧 Quick Fix

Replace the placeholder in `app/src/main/AndroidManifest.xml`:

**Current (Line 22-23):**
```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="YOUR_API_KEY_HERE" />
```

**Replace with:**
```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="AIza..." />  <!-- Your actual API key here -->
```

## 📱 App Package Information

- **Package Name**: `com.example.rush`
- **Application ID**: `com.example.rush`

## ⚙️ Google Cloud Console Configuration

### 1. API Key Restrictions (if any)

If your API key has **Application restrictions** set to "Android apps":

- **Package name**: `com.example.rush`
- **SHA-1 certificate fingerprint**: Get this from your keystore

### 2. Required APIs

Make sure these APIs are enabled in your Google Cloud project:
- ✅ **Maps SDK for Android**
- ✅ **Geocoding API** (optional, for address lookup)
- ✅ **Places API** (optional, for place search)

### 3. Get SHA-1 Fingerprint

For **debug builds** (testing in Android Studio):
```bash
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```

For **release builds** (Play Store):
```bash
keytool -list -v -keystore /path/to/your/release.keystore -alias your_alias_name
```

## 🔍 Common Issues & Solutions

### Issue 1: "This API project is not authorized to use this API"
**Solution**: Enable "Maps SDK for Android" in Google Cloud Console

### Issue 2: "The provided API key is not valid for this application"
**Solutions**:
- Remove application restrictions (temporarily for testing)
- Add correct package name: `com.example.rush`
- Add correct SHA-1 fingerprint from your keystore

### Issue 3: "API key not found"
**Solution**: Verify the API key is correctly pasted in AndroidManifest.xml

### Issue 4: Different package name
If you want to use a different package name, update in:
- `app/build.gradle.kts` → `applicationId`
- Google Cloud Console → API key restrictions

## 🧪 Testing Your Setup

### Method 1: Use the Built-in Debug Screen
The app includes a debug screen that checks:
- ✅ API key detection
- ✅ Permission status  
- ✅ Google Play Services
- ✅ Side-by-side map comparison

### Method 2: Check Android Studio Logs
Look for these messages:
```
✅ RunningMapView: Google Map loaded successfully
❌ RunningMapView: Map loading timeout - switching to fallback
```

### Method 3: Quick Test Script
Add this temporary button to test your API key:

```kotlin
Button(onClick = {
    // This will try to load a simple map
    Log.d("APITest", "Testing Google Maps with current API key")
}) {
    Text("Test API Key")
}
```

## 🚀 Step-by-Step Fix

1. **Copy your working API key** from the other app
2. **Paste it** in `AndroidManifest.xml` replacing `YOUR_API_KEY_HERE`
3. **Check restrictions** in Google Cloud Console
4. **Add package name** `com.example.rush` if restricted
5. **Add SHA-1 fingerprint** if restricted
6. **Clean and rebuild** the app
7. **Test** and check logs

## 📋 Verification Checklist

- [ ] API key replaced in AndroidManifest.xml
- [ ] Maps SDK for Android enabled in Google Cloud
- [ ] Package name `com.example.rush` added to restrictions (if any)
- [ ] SHA-1 fingerprint added to restrictions (if any)
- [ ] App cleaned and rebuilt
- [ ] Tested on device/emulator

## 🆘 Still Not Working?

1. **Temporarily remove all restrictions** from your API key
2. **Test with unrestricted key** - should work immediately
3. **If it works**, gradually add restrictions back
4. **If it doesn't work**, the issue is with API enablement or quota

---

💡 **Pro Tip**: Start with an unrestricted API key for testing, then add restrictions once everything is working!