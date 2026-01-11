# 🔐 PassMan – Secure Password Manager (Android)

[![Android](https://img.shields.io/badge/Android-14+-green.svg)](https://developer.android.com/)
[![Gradle](https://img.shields.io/badge/Build-Gradle-brightgreen.svg)](https://gradle.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.java.net/)

> A secure, feature-rich password manager and file vault for Android with military-grade encryption, biometric authentication, Material Design 3 UI, and comprehensive credential management features.

---

## 📋 Table of Contents

- [About](#about)
- [Features](#features)
- [Requirements](#requirements)
- [Installation](#installation)
- [Building & Running](#building--running)
- [Architecture](#architecture)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Security](#security)
- [Permissions](#permissions)
- [Future Roadmap](#future-roadmap)
- [About the Developer](#about-the-developer)

---

## About

**PassMan Android** is a native Android password manager that provides secure credential storage with military-grade AES-256-CBC encryption. The app combines enterprise-level security with a beautiful, intuitive Material Design 3 interface optimized for mobile devices.

### Key Highlights

- **🔒 Military-Grade Encryption**: AES-256-CBC with PBKDF2 key derivation (100,000+ iterations)
- **📱 Native Android App**: Built with modern Android architecture and best practices
- **🔑 Biometric Authentication**: Fingerprint/Face ID support via BiometricPrompt API
- **🎨 Material Design 3**: Modern, beautiful UI with responsive layouts
- **🌙 Dark Mode**: Full support for Light/Dark/System theme
- **⚡ Fast & Lightweight**: Optimized for performance with minimal resource consumption
- **🔐 Privacy-First**: All data stored locally with SQLite - no cloud sync required
- **📲 Responsive Design**: Optimized for all Android screen sizes (API 26+)

---

## Features

### Authentication & Security

- 🔑 **Biometric Authentication** – Fingerprint/Face ID support via BiometricPrompt API
- 🔐 **Master Password Protection** – SHA-256 + PBKDF2 hashing with salt
- 🔒 **Auto-Lock on Screen Off** – Automatic session termination when device locks
- ⏱️ **Session Management** – Configurable timeout with auto-logout feature
- 🛡️ **Secure Memory Handling** – Sensitive data cleared from memory after use

### User Interface

- 🎨 **Material Design 3** – Modern Material Design 3 interface with MD3 components
- 🌙 **Dark Mode Support** – Full support for Light/Dark/System theme with AppCompatDelegate
- 📲 **Fully Responsive** – Optimized for all Android screen sizes (API 26+)
- ⭐ **Favorites System** – Pin important credentials for instant access
- 🎯 **Intuitive Navigation** – Easy-to-use navigation with bottom navigation

### Credential Management

- 🔐 **Secure Credential Storage** – AES-256-CBC encryption with unique IVs
- 🔍 **Real-time Search** – Instant credential lookup with filter/sort options
- 📋 **Secure Copy** – Copy to clipboard with configurable auto-clear (15 seconds default)
- ✏️ **Full CRUD Operations** – Add, edit, view, and delete credentials
- 📊 **Credential Details** – Comprehensive view with all credential information
- 🎲 **Password Generator** – Customizable password generation with strength controls

### Data Storage & Organization

- 📝 **Secure Notes** – Encrypted notes storage with full edit capabilities
- 💳 **Payment Cards** – Store and manage payment card data securely
- 🔄 **Card Expiration Tracking** – Automated daily expiration checks via WorkManager
- 📧 **Card Expiration Alerts** – Push notifications for expiring cards
- 🗂️ **File Vault** – Encrypted file storage and management
- 🔐 **File Encryption** – Per-file AES-256 encryption with secure handling
- 📸 **Secure Deletion** – Permanent file deletion with secure wiping

### Advanced Features

- 📸 **QR Code Scanner** – Built-in QR scanning for quick credential import
- 🔔 **Smart Notifications** – Card expiration alerts and security warnings
- ⚙️ **Customizable Settings** – Theme, biometric, notification preferences
- 🚀 **App Startup Configuration** – Launcher preferences
- 📊 **Password Strength Analysis** – Real-time password quality feedback

---

## Requirements

### Minimum Requirements
- **Android SDK**: API 26 (Android 8.0 Oreo) or higher
- **Target SDK**: Android 14 (API 34)
- **RAM**: 2 GB minimum (4 GB recommended)
- **Storage**: 50 MB free space
- **Java**: JDK 17 or later (for building)

### Optional Hardware
- Biometric sensor (fingerprint or face recognition) for enhanced security
- Camera for QR code scanning

---

## Installation

### From Android Studio

1. Clone the repository:
```bash
git clone https://github.com/AbirHasanArko/PassMan.git
cd PassMan/android
```

2. Open the `android` folder in Android Studio
3. Let Gradle sync the project
4. Run on emulator or connected device

### Install APK

```bash
# Build and install debug APK
./gradlew installDebug

# Build release APK
./gradlew assembleRelease
```

---

## Building & Running

### Debug Build

```bash
# Build debug APK
./gradlew assembleDebug

# Install on connected device/emulator
./gradlew installDebug

# Run with live debugging
./gradlew runDebug
```

### Release Build

```bash
# Build release APK (unsigned)
./gradlew assembleRelease

# Create signed APK (requires keystore)
./gradlew assembleRelease \
  -Pandroid.injected.signing.store.file=/path/to/keystore.jks \
  -Pandroid.injected.signing.store.password=YOUR_PASSWORD \
  -Pandroid.injected.signing.key.alias=YOUR_ALIAS \
  -Pandroid.injected.signing.key.password=YOUR_KEY_PASSWORD
```

### Run Tests

```bash
# Run all tests
./gradlew test

# Run with coverage
./gradlew testDebugUnitTest --coverage
```

---

## Architecture

PassMan Android follows the **MVVM (Model-View-ViewModel)** pattern with a layered architecture:

```
┌─────────────────────────────────────────┐
│         Presentation Layer (UI)         │
│  ┌──────────────────────────────────┐  │
│  │  Activities & Fragments          │  │
│  │  (SplashActivity, AuthActivity,  │  │
│  │   MainActivity, etc.)            │  │
│  └──────────┬───────────────────────┘  │
└─────────────┼──────────────────────────┘
              │
┌─────────────▼──────────────────────────┐
│      ViewModel Layer (State)           │
│  ┌──────────────────────────────────┐  │
│  │  ViewModels with LiveData        │  │
│  │  (MainViewModel, CardViewModel)  │  │
│  └──────────┬───────────────────────┘  │
└─────────────┼──────────────────────────┘
              │
┌─────────────▼──────────────────────────┐
│     Repository Layer (Business Logic)  │
│  ┌──────────────────────────────────┐  │
│  │  CredentialRepository            │  │
│  │  UserRepository                  │  │
│  │  (Data access abstraction)       │  │
│  └──────────┬───────────────────────┘  │
└─────────────┼──────────────────────────┘
              │
┌─────────────▼──────────────────────────┐
│       Data Layer (Local Storage)       │
│  ┌──────────────────────────────────┐  │
│  │  SQLite Database                 │  │
│  │  DAO (Data Access Objects)       │  │
│  │  Entities                        │  │
│  └──────────────────────────────────┘  │
└──────────────────────────────────────────┘
```

### Design Patterns Used

- **MVVM**: Separation of UI from business logic
- **Repository Pattern**: Abstract data source access
- **DAO Pattern**: Database operations encapsulation
- **Singleton**: Shared service instances (BiometricManager, SessionManager)
- **Observer**: LiveData for reactive UI updates
- **Adapter Pattern**: RecyclerView adapters for lists

---

## Technology Stack

### Core Framework
- **Language**: Java 17 (with core library desugaring for API compatibility)
- **Target SDK**: Android 14 (API 34)
- **Minimum SDK**: Android 8.0 Oreo (API 26)
- **Build Tool**: Gradle 8.5 (Kotlin DSL)

### Architecture & UI
- **Architecture**: MVVM with ViewModel & LiveData
- **UI Framework**: Material Design 3
- **Layout**: XML-based with ConstraintLayout
- **ViewBinding**: Enabled for type-safe views
- **DataBinding**: Support for declarative layouts

### Database & Persistence
- **Local Database**: SQLite with custom DAO layer
- **Entity Management**: Custom entity classes
- **Repository Pattern**: For data abstraction

### Security & Cryptography
- **Encryption**: Java Cryptography Architecture (JCA)
- **Algorithm**: AES-256-CBC with PBKDF2 key derivation
- **Biometric**: BiometricPrompt API (API 28+)
- **Session**: Custom SessionManager with auto-lock

### Networking & Notifications
- **Notifications**: Firebase Cloud Messaging compatible
- **WorkManager**: Background task scheduling (daily card expiration checks)
- **BootReceiver**: Auto-start on device boot

### AndroidX Libraries
- **Core**: androidx.core:core-ktx:1.12.0
- **AppCompat**: androidx.appcompat:appcompat:1.6.1
- **ConstraintLayout**: androidx.constraintlayout:constraintlayout:2.1.4
- **Lifecycle**: lifecycle-viewmodel, lifecycle-livedata, lifecycle-runtime
- **Navigation**: Navigation Component for activity routing
- **Biometric**: androidx.biometric:biometric:1.1.0
- **WorkManager**: androidx.work:work-runtime:2.9.0

### Material Design 3
- **Library**: com.google.android.material:material:1.11.0
- **Components**: Material Design 3 UI components
- **Theme**: Material 3 theme system with dynamic colors

### Build & Testing
- **Testing**: JUnit 5, Mockito
- **ProGuard/R8**: Code obfuscation for release builds
- **Lint**: Gradle-based code analysis

---

## Project Structure

```
PassMan/android/
├── src/main/java/com/passman/android/
│   ├── PassManApp.java                      # Application class
│   ├── SessionManager.java                  # Session management
│   │
│   ├── ui/                                  # Activities & Screens
│   │   ├── splash/
│   │   │   └── SplashActivity.java         # App launch screen
│   │   ├── auth/
│   │   │   ├── AuthActivity.java
│   │   │   └── AuthViewModel.java
│   │   ├── main/
│   │   │   ├── MainActivity.java
│   │   │   ├── MainViewModel.java
│   │   │   ├── CredentialAdapter.java
│   │   │   └── FilterSortBottomSheet.java
│   │   ├── credential/
│   │   │   ├── CredentialDetailActivity.java
│   │   │   ├── AddEditCredentialActivity.java
│   │   │   └── CredentialViewModel.java
│   │   ├── notes/
│   │   │   ├── SecureNotesActivity.java
│   │   │   ├── SecureNoteDetailActivity.java
│   │   │   ├── SecureNoteAdapter.java
│   │   │   └── SecureNotesViewModel.java
│   │   ├── vault/
│   │   │   ├── FileVaultActivity.java
│   │   │   ├── VaultDetailsActivity.java
│   │   │   ├── FileVaultAdapter.java
│   │   │   ├── EncryptedFileAdapter.java
│   │   │   └── FileVaultViewModel.java
│   │   ├── cards/
│   │   │   ├── CardsActivity.java
│   │   │   ├── CardDetailActivity.java
│   │   │   ├── CardAdapter.java
│   │   │   └── CardsViewModel.java
│   │   ├── generator/
│   │   │   ├── PasswordGeneratorActivity.java
│   │   │   └── PasswordGeneratorViewModel.java
│   │   ├── qr/
│   │   │   └── QRScannerActivity.java
│   │   └── settings/
│   │       └── SettingsActivity.java
│   │
│   ├── data/                                # Data persistence layer
│   │   ├── entity/
│   │   │   ├── CredentialEntity.java
│   │   │   ├── UserEntity.java
│   │   │   ├── SecureNoteEntity.java
│   │   │   ├── CardEntity.java
│   │   │   ├── FileVaultEntity.java
│   │   │   └── EncryptedFileEntity.java
│   │   ├── dao/
│   │   │   ├── CredentialDAO.java
│   │   │   ├── UserDAO.java
│   │   │   └── Other entity DAOs
│   │   ├── database/
│   │   │   └── PassManDatabase.java
│   │   └── repository/
│   │       ├── CredentialRepository.java
│   │       └── UserRepository.java
│   │
│   ├── security/                           # Security & cryptography
│   │   ├── BiometricManager.java           # Fingerprint/Face auth
│   │   ├── CryptoManager.java              # AES-256 encryption
│   │   ├── FileEncryptionManager.java
│   │   ├── PasswordStrengthService.java
│   │   └── SessionManager.java
│   │
│   ├── notification/                       # Notifications
│   │   ├── NotificationHelper.java
│   │   └── NotificationScheduler.java
│   │
│   ├── worker/                             # Background tasks
│   │   └── CardExpirationWorker.java
│   │
│   ├── receiver/                           # Broadcast receivers
│   │   ├── ScreenLockReceiver.java
│   │   └── BootReceiver.java
│   │
│   └── util/                               # Utilities
│       └── Various utility classes
│
├── src/main/res/                           # Resources
│   ├── layout/                             # Activity XML layouts
│   ├── drawable/                           # Icons & drawables
│   ├── values/                             # Colors, strings, dimensions
│   │   ├── colors.xml
│   │   ├── strings.xml
│   │   ├── dimens.xml
│   │   ├── themes.xml
│   │   └── styles.xml
│   ├── xml/                                # Network security, file provider
│   │   ├── network_security_config.xml
│   │   ├── data_extraction_rules.xml
│   │   └── file_paths.xml
│   └── mipmap/                             # App icons
│
├── AndroidManifest.xml                     # Manifest with permissions
├── build.gradle.kts                        # Gradle configuration
└── proguard-rules.pro                      # ProGuard rules for release builds
```

---

## Security

### Encryption Architecture

```
Master Password
      ↓
PBKDF2 (100,000 iterations, SHA-256)
      ↓
256-bit Master Key
      ↓
AES-256-CBC Encryption
(Unique IV per entry)
```

### Key Security Features

1. **Zero-Knowledge**: Master password never stored; only salted hash
2. **Per-Entry IVs**: Each encrypted field uses a cryptographically unique IV
3. **Memory Protection**: Sensitive data cleared from memory after use
4. **Secure Random**: Cryptographically strong PRNG for key generation
5. **Biometric Security**: BiometricPrompt API for secure authentication
6. **Auto-Lock**: Automatic session termination on screen off or timeout

### Best Practices

- Never stores master password in plaintext
- All credentials encrypted locally before storage
- Sensitive data cleared from memory immediately after use
- Biometric data never handled directly (delegated to BiometricPrompt)
- Uses Android Keystore for secure key storage when available

---

## Permissions

The app requires the following permissions:

| Permission | Purpose |
|-----------|---------|
| `INTERNET` | Network connectivity (future cloud features) |
| `USE_BIOMETRIC` | Fingerprint/Face authentication |
| `USE_FINGERPRINT` | Legacy fingerprint support (API < 28) |
| `CAMERA` | QR code scanning |
| `VIBRATE` | Haptic feedback |
| `POST_NOTIFICATIONS` | Card expiration alerts (API 33+) |
| `RECEIVE_BOOT_COMPLETED` | Auto-start background scheduler |
| `WRITE_EXTERNAL_STORAGE` | File vault operations (API < 29) |

All permissions are used for security and functionality only. No personal data is collected or shared.

---

## Future Roadmap

### Version 1.0.1 (Current) ✅
- ✅ Core password management with AES-256 encryption
- ✅ Biometric authentication (Fingerprint/Face ID)
- ✅ Credential management (CRUD operations)
- ✅ Secure notes storage
- ✅ Payment card management with expiration tracking
- ✅ File vault with encrypted storage
- ✅ QR code scanner
- ✅ Dark mode support
- ✅ Session management with auto-lock
- ✅ Filter & sort capabilities
- ✅ Material Design 3 UI

### Version 1.1 (Planned)
- [ ] Password strength analysis & recommendations
- [ ] Password reuse detection
- [ ] Enhanced search capabilities
- [ ] Backup & restore functionality
- [ ] Import/export options

### Version 1.5 (Planned)
- [ ] Cloud sync (optional, encrypted)
- [ ] Multi-account support
- [ ] Custom categories for credentials
- [ ] Advanced password analytics
- [ ] Breach checker integration

### Version 2.0+ (Future)
- [ ] Firebase integration for multi-device sync
- [ ] Firebase authentication
- [ ] Breach detection (HaveIBeenPwned API)
- [ ] Password sharing with time limits
- [ ] Advanced security dashboard
- [ ] Browser extension integration
- [ ] Team/family sharing features

---

## About the Developer

**Developer**: Abir Hasan Arko

PassMan is built with a focus on security, usability, and user privacy. The Android app provides a seamless experience for managing credentials securely on mobile devices.

---

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE) file for details.

---

## Support & Feedback

For bug reports, feature requests, or feedback:
- Open an issue on [GitHub](https://github.com/AbirHasanArko/PassMan/issues)
- Check out discussions on [GitHub Discussions](https://github.com/AbirHasanArko/PassMan/discussions)

---

**Built with ❤️ by Abir Hasan Arko**
