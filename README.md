# Smart QR Code Scanner & Generator (Native Android)

A modern, feature-rich native Android application designed for high-speed QR/barcode scanning, multi-category code generation, batch scanning, and subscription-based monetization support.

## 🌟 Key Features
- **Multi-Language Localization**: Support for English, Arabic (عربي), Hindi, Bengali, Japanese, and Chinese.
- **Smart Permission UX**: Clean pre-permission rationale screen ("Camera Access Needed") integrated seamlessly with Android runtime permissions.
- **Comprehensive QR Generator Hub**: Dedicated builder modules for:
  - URL, Wi-Fi, Contact/vCard, Phone, E-mail, Text, SMS, My Card, and Calendar Events.
- **Batch Scan Engine**: Real-time continuous multi-code scanning featuring Pause/Resume controls, image import, and review queue (`Done - Review`).
- **Monetization / Paywall**: High-converting "Go Premium" screen featuring:
  - Ad-free experience, Unlimited Batch Scan, Custom QR Colors & Logos, CSV History Export, and Priority Support.
  - Flexible pricing tiers: Monthly ($1.99) & Yearly ($9.99) with a 3-day free trial.

## 📱 App Flow & Architecture
1. **Onboarding / Locale**: Language selection preference.
2. **Permission Gate**: Non-intrusive camera rationale and system prompt handler.
3. **Creation Suite**: Grid-based category selector for instant QR generation.
4. **Dynamic Data Forms**: Context-aware input validators for Email, Text, vCard/My Card, and Calendar schemas.
5. **Batch Productivity**: Queue-based multi-scan review workflow.
6. **Conversion UI**: Native Billing Client paywall integration.

## 🛠 Tech Stack
- **Language**: Kotlin 
- **UI Architecture**: Native Android Views / XML (or Jetpack Compose)
- **Scanning & Parsing**: CameraX & ZXing Library
- **Billing / Monetization**: Google Play Billing Client
- **State & Data**: Local repository pattern / SharedPreferences / Room (for scan history)

## 📁 Repository Structure Highlights
- `app/src/main/java/`: Core activities, fragments, camera controllers, and generator logic.
- `app/src/main/res/`: XML layouts, vector assets, and multi-language string resources (`values-ar`, `values-hi`, etc.).
