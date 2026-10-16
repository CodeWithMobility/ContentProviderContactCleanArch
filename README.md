# Contacts Provider App

This project is an Android application built using Kotlin, Jetpack Compose, MVVM, Clean Architecture, and Hilt. The app reads phone contacts from the device through the Android contacts provider and displays them in a simple list.

## What has been implemented

### 1. Clean Architecture structure
The project is organized into clear layers:
- `domain/model`: core business model (`Contact`)
- `domain/repository`: repository contract for contact access
- `domain/usecase`: business logic use case (`GetContactsUseCase`)
- `data/repository`: concrete implementation that reads data from Android contacts
- `di`: dependency injection setup using Hilt
- `presentation`: ViewModel and Compose UI

### 2. Contact domain model
A `Contact` model was created to represent a contact with:
- `id`
- `name`
- `phoneNumber`

The model was updated to handle nullable values safely, especially when contact names or phone numbers are missing.

### 3. Repository layer for contacts
The repository implementation accesses Android's contact provider using `ContentResolver` and `ContactsContract.CommonDataKinds.Phone`.

It performs the following:
- queries the phone contacts table
- reads contact ID, display name, and phone number
- filters out invalid or missing rows safely
- returns a list of `Contact` objects

This layer is separated from UI logic to keep the app maintainable and testable.

### 4. Use case layer
A use case class was implemented to wrap repository access:
- `GetContactsUseCase`

This keeps the screen logic focused on UI states rather than low-level data access.

### 5. ViewModel state management
The `ContactViewModel` handles:
- loading state via `isLoading`
- contact data via `contacts`
- error state via `error`
- retry and refresh behavior

It ensures that failed fetches do not leave the app in a confusing state and clears stale error state when a request starts.

### 6. Runtime permission handling
The app requests the Android runtime permission:
- `READ_CONTACTS`

The screen checks whether permission is granted and shows:
- a grant permission prompt when needed
- a retry action if loading fails
- the contact list when permission is available

### 7. Compose UI screen
The `ContactScreen` is built with Jetpack Compose and includes:
- permission prompt
- loading spinner
- contact list display
- empty state message when no contacts are available
- error message with retry button

Each contact row displays:
- contact name
- phone number or fallback text

### 8. Dependency injection with Hilt
Hilt is configured to provide:
- the repository instance
- the use case instance
- the ViewModel wiring

This reduces manual object creation and keeps the app easier to extend.

### 9. Test coverage
Unit tests were implemented for:
- repository success paths
- multiple contact returns
- empty results
- null values
- missing required columns
- error handling behavior in the ViewModel

These tests help validate the app’s main logic and edge cases.

### 10. App setup and execution
To run the project locally:

1. Open the project in Android Studio.
2. Make sure the Android SDK is installed and configured.
3. Sync Gradle files.
4. Run the app on an emulator or connected Android device.

## Project structure summary

```text
app/
├── src/main/java/com/android4you/contactsprovider/
│   ├── data/repository/ContactRepositoryImpl.kt
│   ├── di/ContactModule.kt
│   ├── domain/model/Contact.kt
│   ├── domain/repository/ContactRepository.kt
│   ├── domain/usecase/GetContactsUseCase.kt
│   ├── presentation/ContactScreen.kt
│   ├── presentation/ContactViewModel.kt
│   ├── MainActivity.kt
│   ├── MyApp.kt
│   └── ui/theme/
├── src/test/java/com/android4you/contactsprovider/
│   ├── data/repository/ContactRepositoryImplTest.kt
│   └── presentation/ContactViewModelTest.kt
└── src/main/AndroidManifest.xml
```

## Result
The app now demonstrates a clean, maintainable Android contact-reading flow with proper architecture, permission handling, state management, and test coverage.
