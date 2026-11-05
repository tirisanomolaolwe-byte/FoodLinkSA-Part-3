# FoodLink SA — APK Release and Google Play Publishing Guide

This guide covers a local signed APK for direct installation and an Android App Bundle (AAB) for Google Play. It assumes you are working in the latest local Android Studio project, not the older ZIP snapshot used to prepare these documents.

## 1. Final checks before creating a release

1. Open the latest FoodLink SA project in Android Studio.
2. Confirm the donation, claim and collection screens are present in the local source tree.
3. Decide whether volunteer registration is included. Include it only if it builds and passes Firestore create/read/update testing.
4. Run the tests and build:

   ```powershell
   .\gradlew.bat test
   .\gradlew.bat :app:assembleDebug
   ```

5. Install the debug APK on a physical Android device and test the app.
6. Test with two separate accounts:
   - Account A creates a donation.
   - Account B claims part of that donation.
   - Account B checks My Claims and cancels a reserved claim.
   - Confirm that the donation quantity is restored.
   - Account B claims again.
   - Account A opens the donation and confirms the collection.
   - Check that the claim status and available quantity are correct in Firestore and the app.
7. Test invalid inputs, signed-out navigation, expired deadlines, cancellation and network failures.
8. Review Firestore Security Rules. The service's client-side ownership checks are not sufficient protection by themselves.
9. Confirm the app's privacy policy, account deletion support, notification behaviour and language resources.

Do not create a public release from a build that has only been compiled but not installed and tested.

## 2. Check the Android application ID and version

The inspected project snapshot currently has:

```kotlin
applicationId = "com.example.foodlinksa"
minSdk = 24
targetSdk = 36
versionCode = 1
versionName = "1.0"
```

Before the first Play Store upload:

- Replace `com.example.foodlinksa` with a unique, permanent application ID owned by the project/team. Do this before creating the Play Console app record.
- Register the exact same package/application ID as an Android app in Firebase.
- Download the matching `google-services.json` and put it in the `app/` directory.
- Recheck Google sign-in OAuth configuration and signing fingerprints after generating the release key.
- Keep the app's `namespace` and `applicationId` consistent with the intended package structure. Do not make package changes casually after publishing.

The inspected configuration already targets API 36. Google Play's requirement for new Android phone apps and updates changed on 31 August 2026 to Android 16 / API 36 or higher. Recheck the current value in the local file before uploading.

For every new upload after the first release, increment `versionCode`. Do not reuse an already uploaded version code.

## 3. Generate a debug APK for device testing

In Android Studio:

1. Select **Build**.
2. Select **Build Bundle(s) / APK(s)**.
3. Select **Build APK(s)**.
4. Wait for the build to finish and choose **Locate** in the notification.

Or run:

```powershell
.\gradlew.bat :app:assembleDebug
```

Expected output:

```text
app/build/outputs/apk/debug/app-debug.apk
```

This is a debug build. Use it for development, lecturer demonstration or controlled testing only. Do not label it as the signed Play Store release.

## 4. Generate a signed APK for direct distribution

1. In Android Studio, select **Build > Generate Signed Bundle / APK**.
2. Choose **APK** and continue.
3. Select an existing keystore or create a new keystore.
4. Store the `.jks` or `.keystore` file somewhere safe outside the repository (for example, a secured backup location).
5. Record the key alias and passwords in a password manager. Do not put passwords in Git, README files or screenshots.
6. Select the `release` build variant and the signing options offered by Android Studio.
7. Finish the wizard and wait for the signed build to complete.
8. Use the notification to locate the signed APK. Its output is normally under:

   ```text
   app/build/outputs/apk/release/
   ```

9. Install the signed APK on a test device and verify login, navigation, Firestore operations and collection tracking.
10. Keep a copy of the exact signed APK and record its version code and checksum for your submission evidence.

Back up the upload key securely. Losing the key can complicate future updates. Never commit the keystore or its credentials to GitHub.

## 5. Generate the Google Play App Bundle (AAB)

New apps on Google Play are required to publish using Android App Bundles. An APK can still be generated for direct installation, but use an AAB for a new Play Store app.

1. Select **Build > Generate Signed Bundle / APK** in Android Studio.
2. Choose **Android App Bundle**.
3. Select the `release` build variant.
4. Choose or create the upload keystore.
5. Enable Play App Signing when prompted in Play Console for the first release.
6. Finish the build.

Expected output is normally:

```text
app/build/outputs/bundle/release/app-release.aab
```

Upload this AAB to Play Console. Google Play App Signing manages the app-signing key used for delivered APKs; your local keystore is the upload key. Keep the upload key safe and backed up.

## 6. Prepare the Google Play developer account

1. Open [Google Play Console](https://play.google.com/console/).
2. Register or sign in with the Google account that should own the app.
3. Accept the Developer Distribution Agreement and complete identity/contact verification.
4. Google currently lists a one-time US$25 developer registration fee.
5. Choose the correct account type (Personal or Organization). Do not select Organization unless the team has the required organization identity and verification information.
6. If this is a new personal developer account created after 13 November 2023, check the Play Console testing requirements. Google currently requires a closed test with at least 12 opted-in testers continuously for 14 days before applying for production access. New personal accounts may also need to verify access to an Android device through the Play Console mobile app.

## 7. Create the app in Play Console

1. Select **Create app**.
2. Enter the app name: **FoodLink SA**.
3. Select the default language.
4. Select **App** (not Game), and choose free or paid appropriately.
5. Confirm the declarations and create the app.
6. Complete the store listing:
   - App name and short description.
   - Full description.
   - App icon and feature graphic.
   - Phone screenshots that show the actual app.
   - Support email and any required contact information.
   - Privacy policy URL.
7. Complete the App content declarations, including content rating, target audience, ads declaration, app access instructions and Data safety.
8. Provide working reviewer login details if app features require authentication. Use a dedicated test account, not a personal account.
9. Make sure the screenshots and descriptions do not promise features that are not available in the uploaded build.

## 8. Privacy, Data safety and account deletion

FoodLink SA uses account authentication and Firestore records. The Data safety form must accurately describe the data handled by the app and any third-party SDKs. Do not guess the answers: inspect the final app, Firebase configuration and SDK behaviour first.

Before publishing:

- Publish a privacy policy on a stable public URL and link it in Play Console. The policy must explain what data is collected, why it is used, how it is shared, how it is protected and how users can contact the team.
- Complete the Play Console Data safety form accurately.
- If users can create an account in the app, provide a discoverable in-app way to request account deletion and a separate working external web page/URL for account-deletion requests. The deletion process must cover associated user data, subject to clearly disclosed lawful retention requirements.
- Check that Firestore rules prevent users from reading or altering another user's private records and that donor-only actions are enforced by rules, not just hidden buttons.
- Check notification permissions and explain notification use where required.

Account deletion is a significant release blocker if the app permits account creation and does not yet provide both required request paths.

## 9. Upload and test the release

1. In Play Console, open **Test and release > Testing > Internal testing**.
2. Create an internal testing release.
3. Upload the signed `.aab` file.
4. Add a concise release name and release notes.
5. Resolve any Play Console errors and warnings.
6. Add testers and share the opt-in link.
7. Install the Play-delivered build and repeat the core donor/recipient smoke test.
8. For applicable new personal accounts, complete the closed-testing requirement before applying for production access.
9. When ready, create the production release, upload the same tested release artifact (or a higher version code if it changed), complete review and submit for publishing.

Google review and publication are controlled by Google Play. Uploading a bundle does not mean that the app has been approved or published.

## 10. Suggested release notes for Play Console

> FoodLink SA helps users discover meals and redistribute surplus food. This release includes account registration and login, meal browsing and search, saved favourites, user preferences, donation listing management, quantity-based food reservations, claim tracking, reservation cancellation and donor collection confirmation. It also includes local meal-list caching and multilingual resource support. This is an academic prototype; some integrations may require project configuration and are being validated before wider distribution.

Edit this text so that it matches the exact build uploaded to Play Console.

## Official Google references

- [Target API level requirements](https://support.google.com/googleplay/android-developer/answer/11926878)
- [Prepare and roll out a release](https://support.google.com/googleplay/android-developer/answer/9859348)
- [Get started with Play Console](https://support.google.com/googleplay/android-developer/answer/6112435)
- [Prepare your app for review](https://support.google.com/googleplay/android-developer/answer/9859455)
- [Data safety form](https://support.google.com/googleplay/android-developer/answer/10787469)
- [Account deletion requirements](https://support.google.com/googleplay/android-developer/answer/13327111)
- [App preview assets](https://support.google.com/googleplay/android-developer/answer/9866151)
