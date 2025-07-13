#!/bin/bash

echo "🔑 Getting SHA-1 Fingerprint for Google Maps API Key"
echo "=================================================="
echo ""

echo "📱 For DEBUG builds (testing in Android Studio):"
echo "Run this command on your development machine:"
echo ""
echo "keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android"
echo ""

echo "🏪 For RELEASE builds (Play Store):"
echo "Run this command with your release keystore:"
echo ""
echo "keytool -list -v -keystore /path/to/your/release.keystore -alias your_alias_name"
echo ""

echo "📋 What to look for:"
echo "Look for a line like:"
echo "SHA1: AA:BB:CC:DD:EE:FF:00:11:22:33:44:55:66:77:88:99:AA:BB:CC:DD"
echo ""

echo "📝 Copy the SHA1 value and add it to your Google Cloud Console:"
echo "1. Go to Google Cloud Console"
echo "2. Navigate to APIs & Services → Credentials"
echo "3. Click on your API key"
echo "4. Under 'Application restrictions' → 'Android apps'"
echo "5. Add package name: com.example.rush"
echo "6. Add the SHA-1 fingerprint you just copied"
echo ""

echo "🚀 Quick Test (No Restrictions):"
echo "For initial testing, you can temporarily remove all restrictions"
echo "from your API key to verify it works, then add them back."
echo ""

# Try to automatically detect and show SHA-1 if on a typical dev machine
if [ -f ~/.android/debug.keystore ]; then
    echo "🎉 Found debug keystore! Here's your SHA-1:"
    echo ""
    keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android 2>/dev/null | grep SHA1 | head -1
    echo ""
else
    echo "ℹ️  Debug keystore not found at ~/.android/debug.keystore"
    echo "   Make sure you've built an Android app before on this machine"
    echo ""
fi

echo "📱 Your app package name: com.example.rush"
echo "🔧 Don't forget to replace YOUR_API_KEY_HERE in AndroidManifest.xml!"