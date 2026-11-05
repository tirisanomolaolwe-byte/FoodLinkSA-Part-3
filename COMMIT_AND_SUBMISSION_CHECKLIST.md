# FoodLink SA — Commit and Submission Checklist

## 1. Confirm the current Git state

Open the project root in Android Studio Terminal or PowerShell:

```powershell
git status
git branch --show-current
git log --oneline -8
```

Review the changed-file list before staging. The current branch noted in the previous Git output was `feature/donation-foundation`, but confirm the branch locally before committing.

## 2. Commit the remaining implementation

If Milestones 1 and 2 are already committed, do not recreate those commits. Review `git status` and stage only the new, intended source files for the donation claiming and collection work, volunteer work that has passed a build, and the updated navigation/resources.

Suggested feature commit:

```powershell
git add app/src/main/java/com/example/foodlinksa/data/model/DonationClaim.kt
git add app/src/main/java/com/example/foodlinksa/data/repository/DonationClaimDatabaseService.kt
git add app/src/main/java/com/example/foodlinksa/ui/donation
git add app/src/main/res/layout app/src/main/res/navigation app/src/main/res/values app/src/main/res/values-af app/src/main/res/values-zu app/src/main/res/menu
git status
git commit -m "Implement donation claiming and collection tracking"
```

Important: this is a suggested staging example. Check every path shown by `git status` and do not stage unrelated files or generated build output. If the volunteer files are not present or have not passed a build, do not commit them as a completed feature.

## 3. Add the documentation

Copy the supplied documentation files into the project root:

- `README.md`
- `RELEASE_NOTES.md`
- `RELEASE_AND_PUBLISHING.md`
- `COMMIT_AND_SUBMISSION_CHECKLIST.md`

Then run:

```powershell
git add README.md RELEASE_NOTES.md RELEASE_AND_PUBLISHING.md COMMIT_AND_SUBMISSION_CHECKLIST.md
git status
git commit -m "Update project documentation and release notes"
```

## 4. Final verification before push

```powershell
.\gradlew.bat test
.\gradlew.bat :app:assembleDebug
```

Install the final debug build and test the donation flow on a physical device. Then produce the signed APK/AAB using `RELEASE_AND_PUBLISHING.md`.

Before `git push`, check that no keystore, signing password, local.properties file, private service-account key or generated build output is staged. Push only after confirming that the commits contain the intended files.

## 5. Submission evidence to capture

- Git branch and commit history showing the completed milestones.
- Android Studio build output showing a successful final build.
- Unit test output.
- Login/registration screens and successful authentication.
- Meal browsing, search, details and favourites.
- Donor creating a donation and viewing My Donations.
- Recipient claiming a partial quantity and viewing My Claims.
- Recipient cancelling a reserved claim and the restored quantity.
- Donor confirming collection and the updated claim status.
- Volunteer application screen and successful save/update only if the feature is included in the final build.
- APK installation on a physical device.
- Signed release APK/AAB output and version details.
- Play Console testing/release status only if an upload is actually completed.

Do not state that the app is published on Google Play unless Play Console confirms that the production release has been approved and rolled out.
