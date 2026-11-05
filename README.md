# FoodLinkSA

FoodLinkSA is an Android application developed as a collaborative group project. The application provides food and meal-related functionality through an Android mobile interface and integrates external services to support its functionality.

## Overview

The project is developed as an Android application using Kotlin and Gradle. It includes user authentication, meal discovery, meal searching, meal information, and integration with external services.

This repository contains the project's source code, Gradle configuration, automated tests, and continuous integration configuration.

## Features

* User registration and authentication
* User login
* Meal and food discovery
* Meal searching
* Viewing meal information and details
* User-related functionality
* Firebase integration
* External API integration
* Image loading for meal content

## Technology Stack

* **Language:** Kotlin
* **Platform:** Android
* **Build System:** Gradle
* **UI:** Android components and XML layouts
* **Authentication:** Firebase Authentication
* **Cloud Services:** Firebase
* **Networking:** Retrofit
* **Image Loading:** Coil
* **Testing:** JUnit and Android instrumented testing
* **Version Control:** Git and GitHub
* **Continuous Integration:** GitHub Actions

## Project Structure

```text
FoodLinkSA/
├── app/
│   └── src/
│       ├── main/
│       ├── test/
│       └── androidTest/
├── gradle/
│   └── wrapper/
├── .github/
│   └── workflows/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradlew
└── gradlew.bat
```

## Testing

The project contains automated tests using JUnit and Android testing frameworks.

Unit tests are located in:

```text
app/src/test/
```

Android instrumented tests are located in:

```text
app/src/androidTest/
```

Tests can be executed locally with:

```bash
./gradlew test
```

On Windows PowerShell or Git Bash:

```bash
./gradlew test
```

The project can be built with:

```bash
./gradlew build
```

A debug APK can be generated with:

```bash
./gradlew assembleDebug
```

## Continuous Integration

GitHub Actions is used to automate the testing and build process.

The planned continuous integration workflow will:

1. Check out the source code.
2. Configure the required Java and Android environment.
3. Run the automated tests.
4. Build the Android application.
5. Generate a debug APK.
6. Report whether the tests and build completed successfully.

This provides an automated verification process that does not depend solely on an individual developer's local development environment.

## Firebase Configuration

The application uses Firebase services for application functionality.

Local Android configuration files such as `local.properties` are excluded from version control because they contain machine-specific configuration.

Developers working with the project should configure the required Android SDK and Firebase environment according to the project's development requirements.

## Getting Started

Clone the repository:

```bash
git clone <repository-url>
```

Open the project in Android Studio and allow Gradle to synchronise the project.

Ensure that the required Android SDK and development tools are installed.

The project can then be built using:

```bash
./gradlew build
```

## GitHub Actions

The GitHub Actions workflow will be located at:

```text
.github/workflows/build.yml
```

The workflow will automatically run when changes are pushed to the repository and when pull requests are created against the main branch.

## Project Status

FoodLinkSA is being developed as a collaborative academic software-development project.

The repository is used to manage the application's source code, documentation, automated testing, and continuous integration process.

## Contributors
Tirisano Molaolwe 
Katlego Kwakwari
Katlego Makhuba

FoodLinkSA is a collaborative group project developed by the project team.
