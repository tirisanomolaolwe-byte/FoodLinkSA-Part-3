# FoodLink SA — Release Notes

## Version 1.0 — Submission candidate

**Release date:** 5 November 2026  
**Application version in the inspected Gradle configuration:** `1.0` (`versionCode` 1)  
**Release type:** Academic prototype / submission candidate

FoodLink SA has been extended from a meal-discovery application into a food donation and collection prototype. The release combines the existing meal features with a donor-to-recipient workflow for surplus food.

## Main updates

### 1. Account and profile functionality

- Firebase Authentication for email/password accounts.
- Google sign-in integration using Android Credential Manager and Firebase Authentication.
- Firestore-backed user profile and preference information.
- Settings for profile information and preferred meal category.

### 2. Meal discovery improvements

- Browse meals by category.
- Search for meals and view detailed meal information through TheMealDB.
- Save and retrieve favourite meals using Firestore.
- Load meal thumbnails and detail images through Coil.
- Cache previously loaded category meal lists locally so that the app can fall back to saved results when the API is unavailable.

### 3. Food record database operations

- Firestore-backed food item records.
- Create, retrieve, update and delete operations through a dedicated database service.

### 4. Food donation management

- Create and publish surplus-food donation records.
- Browse available and partially claimed donations.
- View donation details, including quantity, collection address and collection deadline.
- Donors can view, edit and cancel eligible donation records.
- A donor's own listings are separated from the general donation feed.

### 5. Donation claiming and collection tracking

- Recipients can reserve a selected quantity from an available donation.
- Firestore transactions update the remaining quantity and create the associated claim record atomically.
- The transaction checks that the donation still has enough available quantity, helping prevent over-claiming when multiple recipients attempt to reserve food.
- Recipients can view their claims.
- Recipients can cancel reserved claims; the cancelled quantity is returned to the donation's available balance.
- Donors can view claims associated with their own donation and confirm when an individual collection is complete.
- Claim statuses distinguish `RESERVED`, `CANCELLED` and `COLLECTED`.

## Innovative aspects

- **Quantity-based reservations:** Recipients do not need to reserve an entire donation. They can request only the amount they need.
- **Transactional stock control:** The donation quantity update and claim creation take place in one Firestore transaction, rather than relying on separate client-side updates.
- **Recoverable reservations:** Cancelling an eligible claim restores the reserved quantity to the donation.
- **Collection confirmation:** Collection is tracked per claim, which supports multiple recipients collecting portions of one donation.
- **Separated donor and recipient views:** Donors manage their listings and confirm collections, while recipients manage their own claims.

## Languages and notifications

- English, Afrikaans and isiZulu resource folders are included.
- Firebase Cloud Messaging receiver and notification-channel handling are included. End-to-end notification delivery and token registration must still be verified before notifications are described as a fully working production feature.

## Volunteer registration work

Volunteer registration model, Firestore service, form layout, Fragment and navigation/menu integration were prepared during the submission work. This feature should be treated as part of the release only after a successful local build and Firestore create/read/update test. It is not listed as a verified production capability in this release note.

## Deferred or not verified for this release

- Impact Dashboard / statistics screen (Milestone 6) — intentionally dropped from the submission scope.
- Full Firestore Security Rules audit and deployment — must be completed before public distribution.
- End-to-end two-account donor/recipient runtime test — required; compilation alone is insufficient.
- Complete FCM token registration and server-side notification triggering — not verified.
- Firebase Storage upload support for donation photos — not verified.
- Account deletion flow and external account-deletion request page — must be added before Google Play publication if account creation is available.

## Build and test status

- The latest reported debug build after donor claim/collection management was successful.
- Earlier project test documentation records three passing `AuthUtilsTest` unit tests. Re-run the tests on the exact final source tree before creating the release candidate.
- Final release APK/AAB signing and installation verification remain release steps; no signed release binary is represented by this document.
