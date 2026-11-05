# FoodLink SA

FoodLink SA is an Android application prototype focused on food discovery and the redistribution of surplus food. It combines meal discovery with a donor-to-recipient workflow so that available food can be listed, reserved in quantities, and confirmed as collected.

The project is developed in Kotlin using Android Studio, XML layouts, Firebase services and an external meal API.

## Project objectives

FoodLink SA aims to make it easier to:

- Discover meals and food information.
- Manage a user's account preferences and saved meals.
- List surplus food that is available for collection.
- Allow recipients to reserve all or part of a donation.
- Track reservations and confirm completed collections.

## Implemented functionality

### Accounts and user preferences

- Email and password registration and login through Firebase Authentication.
- Google sign-in integration using Android Credential Manager and Firebase Authentication. The Google provider, OAuth client configuration and signing fingerprints must be enabled correctly in the Firebase project before this can be used in a release build.
- User profile and preference storage in Cloud Firestore.
- Settings for profile information and preferred meal category.

### Meal discovery

- Meal browsing by category.
- Meal search and meal detail screens using TheMealDB REST API.
- Meal thumbnails and images loaded with Coil.
- A favourites feature backed by Firestore.
- Basic offline fallback for previously loaded category lists using a local SharedPreferences cache (`FoodCache`). This is a category-list cache, not a full Room database implementation.

### Food records

- A Firestore-backed food item model and database service with create, retrieve, update and delete operations for user-created food records.

### Food donation and collection workflow

The donation workflow is the main FoodLink SA extension beyond meal discovery.

- Donors can create surplus-food donation listings.
- Donors can view their own donation listings, edit eligible listings and cancel eligible listings.
- Recipients can browse available and partially claimed donations and open donation details.
- Recipients can reserve a requested quantity rather than having to claim the entire donation.
- Donation quantities and the corresponding claim record are updated together in a Firestore transaction. This reduces the risk of two recipients reserving the same remaining quantity.
- Recipients can view their claims and cancel a reservation that is still reserved. Cancelling restores the reserved quantity to the original donation.
- Donors can view claims made against their own donations and confirm a collection. The collection status is recorded against the individual claim so that one collection does not incorrectly mark every claim for a donation as collected.

The primary Firestore collections used by this workflow are `donations` and `donationClaims`.

### Notifications and languages

- Firebase Cloud Messaging service integration and notification-channel display handling are included in the project. End-to-end message delivery, token registration and any server-side notification trigger still need to be verified; the current `onNewToken` handler does not upload the token to a backend.
- English, Afrikaans and isiZulu string resources are present in the project. Check the current settings/locale flow on the target device before demonstrating language switching.

## Technology stack

| Area | Technology |
|---|---|
| Android application | Kotlin, Android SDK |
| User interface | XML layouts, View Binding, Material Components |
| Navigation | AndroidX Navigation Component |
| Authentication | Firebase Authentication |
| Cloud database | Cloud Firestore |
| Notifications | Firebase Cloud Messaging |
| External meal data | TheMealDB REST API |
| Networking | Retrofit, OkHttp, Kotlin coroutines |
| Image loading | Coil |
| Local meal cache | SharedPreferences with Gson serialization |
| Automated tests | JUnit and AndroidX test libraries |
| Build system | Gradle Kotlin DSL |

## Architecture overview

The app is organised by responsibility:

```text
com.example.foodlinksa/
├── MainActivity.kt
├── data/
│   ├── api/          # Retrofit API interface and client
│   ├── local/        # Local cache and local data components
│   ├── model/        # Meal, food, donation and claim data models
│   └── repository/   # API and Firestore data access
├── ui/
│   ├── auth/         # Login and registration
│   ├── food/         # Meal browsing, favourites and details
│   ├── donation/     # Donation, claim and collection screens
│   ├── volunteer/    # Volunteer registration work, if included in the final build
│   └── settings/     # User settings
└── util/             # Authentication and Firebase messaging utilities
```

The layout and navigation resources are under `app/src/main/res/`.

## Requirements

Use the Android Studio version and SDK components compatible with the project's Gradle and Android Gradle Plugin configuration. The inspected project configuration uses:

- JDK 21 Gradle toolchain.
- Gradle 9.5 wrapper.
- Android Gradle Plugin 9.3.2.
- `compileSdk` 37.
- `targetSdk` 36.
- `minSdk` 24.

The local project is the source of truth. Confirm the values in `app/build.gradle.kts` before creating a release.

## Firebase setup

1. Create or use a Firebase project.
2. Register an Android app using the exact `applicationId` from `app/build.gradle.kts`.
3. Place the matching `google-services.json` file in the `app/` directory.
4. Enable the Firebase Authentication providers required by the application (Email/Password and Google, if Google sign-in is being demonstrated).
5. Create the Cloud Firestore database and deploy restrictive Firestore Security Rules that match the app's actual data model.
6. For Google sign-in, configure the OAuth client and add the relevant SHA-1/SHA-256 signing certificate fingerprints to Firebase.
7. Test the app with separate donor and recipient accounts.

Do not commit release keystores, private signing credentials, service-account JSON files or passwords. A Firebase `google-services.json` file contains project identifiers and API configuration; restrict its API key and secure Firebase data through Authentication and Firestore Security Rules. It is not a replacement for database security rules.

## Build and test

Open the project folder in Android Studio and allow Gradle synchronisation to complete.

On Windows PowerShell:

```powershell
.\gradlew.bat test
.\gradlew.bat :app:assembleDebug
```

On macOS/Linux:

```bash
./gradlew test
./gradlew :app:assembleDebug
```

The debug APK is normally generated at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

A debug APK is intended for development and testing. For a distributable release, generate a signed APK or, for Google Play, a signed Android App Bundle (AAB). See [RELEASE_AND_PUBLISHING.md](RELEASE_AND_PUBLISHING.md).

## Current release scope and limitations

- The Impact Dashboard (Milestone 6) has been deliberately dropped from the current submission scope.
- Volunteer registration files and navigation were prepared during the current development session. Include this feature in the release only after confirming that the files are present in the local project, the app builds, and create/read/update operations work against Firestore.
- Donation and claim functionality has passed the reported debug build. A successful build does not prove that the full donor/recipient workflow works against the live Firestore project; perform the two-account smoke test before submission.
- Review and deploy Firestore Security Rules before sharing the app. Client-side ownership checks alone do not secure Firestore data.
- Confirm end-to-end FCM delivery. The current token callback is empty, so automatic targeted notifications should not be advertised as fully operational without an additional token-registration and sending mechanism.
- Confirm that the app supports account deletion. Google Play requires an in-app way to request account deletion and a working external deletion-request web resource for apps that allow account creation.
- Confirm that the app has an appropriate privacy policy and that the Play Console Data safety declarations match the app and its third-party SDKs.
- The inspected project configuration uses the sample application ID `com.example.foodlinksa`. Choose a unique permanent application ID before the first Google Play upload, then register that same ID in Firebase and download a matching `google-services.json`.
- Donation models contain an image URL field, but a Firebase Storage upload workflow was not verified as part of the current implementation.

## Contributors

- Tirisano Molaolwe
- Katlego Kwakwari
- Katlego Makhuba

FoodLink SA is a collaborative academic project.
