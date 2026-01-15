# 🔐 PassMan: All-in-One Security Solution

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.java.net/)
[![JavaFX](https://img.shields.io/badge/JavaFX-21-blue.svg)](https://openjfx.io/)
[![Android](https://img.shields.io/badge/Android-14+-green.svg)](https://developer.android.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Build](https://img.shields.io/badge/Build-Gradle-brightgreen.svg)](https://gradle.org/)

> A comprehensive, cross-platform password management and file vault solution with military-grade encryption, available as native Android and JavaFX desktop applications, sharing a common encrypted core module.

---

## ⬇️ Downloads

Get the latest installers for your platform:

| Platform | Installer | Version |
|----------|-----------|---------|
| 📱 **Android** | [PassMan.apk](../../releases) | Latest Release |
| 🪟 **Windows** | [PassMan.exe](../../releases) | Latest Release |

**👉 [View all releases →](../../releases)**

---

## 📋 Quick Navigation

### 🚀 Quick Links
- **[Android App README](android/README.md)** – Build, run, and develop the Android app
- **[Desktop App README](desktop/README.md)** – Build, run, and develop the JavaFX desktop app
- **[Architecture Documentation](#architecture)** – How modules work together
- **[Getting Started](#getting-started)** – Set up the entire project

---

## 📱 Applications

### Android Application

A native Android password manager with Material Design 3 UI, biometric authentication, and comprehensive credential management.

**Features:**
- 🎨 Material Design 3 interface
- 🔑 Biometric authentication (Fingerprint/Face ID)
- 💳 Payment card management
- 🗂️ Encrypted file vault
- 📝 Secure notes storage
- 🎲 Password generator
- 🌙 Dark mode support

**[→ Android README →](android/README.md)**

**Build & Run:**
```bash
cd android
./gradlew assembleDebug      # Build debug APK
./gradlew installDebug       # Install on device
./gradlew assembleRelease    # Build release APK
```

---

### Desktop Application (JavaFX)

A cross-platform desktop password manager for Windows, macOS, and Linux with advanced features and professional UI.

**Features:**
- 🖥️ Cross-platform JavaFX UI
- 📊 Advanced security analytics
- 🔐 Credential management dashboard
- 💾 Local backup & restore
- 📈 Password health analysis
- 👤 Admin dashboard
- 🎮 Gamification support

**[→ Desktop README →](desktop/README.md)**

**Build & Run:**
```bash
cd desktop
./gradlew run                # Run application
./gradlew build              # Build JAR
./gradlew jpackage          # Create native installer
```

---

## 🏗️ Project Architecture

PassMan uses a **multi-module architecture** with shared core logic:

```
┌────────────────────────────────────────────────────────────┐
│                   Presentation Layer                        │
│  ┌──────────────────────┐  ┌──────────────────────────┐   │
│  │  Android (Kotlin DSL) │  │  Desktop (JavaFX)        │   │
│  │  ├─ Material Design 3 │  │  ├─ FXML UI             │   │
│  │  ├─ Activities       │  │  ├─ JavaFX Controllers  │   │
│  │  ├─ ViewModels       │  │  ├─ CSS Styling        │   │
│  │  └─ LiveData         │  │  └─ Scene Management   │   │
│  └──────────┬───────────┘  └───────────┬──────────────┘   │
└─────────────┼──────────────────────────┼──────────────────┘
              │                          │
              └──────────┬───────────────┘
                         │
┌────────────────────────▼──────────────────────────────────┐
│                    Core Module                            │
│  ┌──────────────────────────────────────────────────┐   │
│  │         Shared Business Logic (Pure Java)        │   │
│  │  ├─ EncryptionService (AES-256-CBC)             │   │
│  │  ├─ PasswordAnalysisService                      │   │
│  │  ├─ BackupService                                │   │
│  │  ├─ PasswordGenerator                            │   │
│  │  ├─ SessionManager                               │   │
│  │  └─ DatabaseManager (SQLite)                     │   │
│  └──────────┬───────────────────────────────────────┘   │
│             │                                             │
│  ┌──────────▼───────────────────────────────────────┐   │
│  │        Repository & DAO Layer                   │   │
│  │  ├─ CredentialRepository                        │   │
│  │  ├─ CredentialDAO                               │   │
│  │  ├─ UserRepository                              │   │
│  │  └─ EntityClasses                               │   │
│  └──────────┬───────────────────────────────────────┘   │
│             │                                             │
│  ┌──────────▼───────────────────────────────────────┐   │
│  │           Data Layer (SQLite)                   │   │
│  │  ├─ Connection Pool & Management                │   │
│  │  ├─ Encrypted Storage                           │   │
│  │  └─ Database Migrations                         │   │
│  └──────────────────────────────────────────────────┘   │
└───────────────────────────────────────────────────────────┘
```

### Module Breakdown

| Module | Purpose | Language | Framework |
|--------|---------|----------|-----------|
| **core** | Shared encryption, services, database | Java 17 | Pure Java |
| **android** | Mobile app | Java 17 | AndroidX, Material Design 3 |
| **desktop** | Desktop app | Java 17 | JavaFX 21 |

---

## 🔒 Security Architecture

All data is encrypted using **AES-256-CBC** with **PBKDF2** key derivation:

```
Master Password (User Input)
         ↓
PBKDF2 (100,000 iterations, SHA-256)
         ↓
256-bit Master Key
         ↓
├─ AES-256-CBC (Credentials)
├─ AES-256-CBC (Secure Notes)
├─ AES-256-CBC (Payment Cards)
├─ AES-256-CBC (File Vault)
└─ Each entry has unique IV
```

**Key Features:**
- ✅ Master password never stored in plaintext
- ✅ Per-entry encryption with unique IVs
- ✅ Secure key derivation (100,000 iterations)
- ✅ Memory protection (clear sensitive data)
- ✅ Secure random number generation
- ✅ Zero-knowledge architecture

---

## 🛠️ Technology Stack

### Core Technologies

| Component | Technology | Version |
|-----------|-----------|---------|
| **Language** | Java | 17 LTS |
| **Database** | SQLite | 3.43+ |
| **Encryption** | JCA (Java Cryptography Architecture) | JDK Built-in |
| **Build Tool** | Gradle | 8.5 (Kotlin DSL) |

### Android-Specific

| Component | Library | Version |
|-----------|---------|---------|
| **UI Framework** | Material Design 3 | 1.11.0 |
| **Lifecycle** | AndroidX Lifecycle | 2.7.0 |
| **Database** | SQLite (Custom DAO) | 3.43+ |
| **Authentication** | BiometricPrompt | 1.1.0 |
| **Background Tasks** | WorkManager | 2.9.0 |

### Desktop-Specific

| Component | Library | Version |
|-----------|---------|---------|
| **UI Framework** | JavaFX | 21.0.1 |
| **Scene Management** | Custom SceneManager | - |
| **QR Codes** | ZXing | 3.5.2 |
| **Packaging** | jpackage | JDK 17+ Built-in |

---

## 📁 Project Structure

```
PassMan/
├── core/                          # Shared core module
│   ├── src/main/java/com/passman/core/
│   │   ├── crypto/                # Encryption utilities
│   │   ├── db/                    # Database layer
│   │   ├── repository/            # Data access layer
│   │   ├── services/              # Business logic services
│   │   ├── model/                 # Domain entities
│   │   ├── generator/             # Password generation
│   │   ├── analysis/              # Security analysis
│   │   └── util/                  # Utilities
│   ├── build.gradle.kts
│   └── README.md
│
├── android/                       # Android application
│   ├── src/main/java/com/passman/android/
│   │   ├── ui/                    # Activities & Screens
│   │   ├── data/                  # Entities & Repositories
│   │   ├── security/              # Biometric & Session
│   │   ├── worker/                # Background tasks
│   │   └── util/                  # Utilities
│   ├── src/main/res/              # Resources (layouts, drawables, etc)
│   ├── build.gradle.kts
│   ├── AndroidManifest.xml
│   └── README.md
│
├── desktop/                       # Desktop JavaFX application
│   ├── src/main/java/com/passman/desktop/
│   │   ├── ui/                    # Controllers & ViewModels
│   │   ├── utils/                 # Utilities
│   │   └── MainApp.java           # Entry point
│   ├── src/main/resources/
│   │   ├── fxml/                  # FXML layout files
│   │   ├── styles/                # CSS stylesheets
│   │   └── icons/                 # Application icons
│   ├── build.gradle.kts
│   └── README.md
│
├── gradle/                        # Gradle wrapper
├── build.gradle.kts               # Root Gradle configuration
├── settings.gradle.kts            # Multi-module setup
├── README.md                      # This file
├── LICENSE                        # MIT License
└── .gitignore
```

---

## 🚀 Getting Started

### Prerequisites

- **Java Development Kit (JDK)**: 17 or later
- **Gradle**: 8.0+ (wrapper included)
- **Git**: For version control
- **Android Studio** (Optional): For Android development
- **IDE**: IntelliJ IDEA, Eclipse, or VS Code (recommended)

### Clone Repository

```bash
git clone https://github.com/AbirHasanArko/PassMan.git
cd PassMan
```

### Verify Installation

```bash
# Check Java version
java -version          # Should be 17+

# Check Gradle
./gradlew --version   # Should be 8.0+

# List all modules
./gradlew projects
```

---

## 🔨 Building All Modules

### Build Core Module

```bash
# Build core library
./gradlew :core:build

# Run core tests
./gradlew :core:test

# Generate documentation
./gradlew :core:javadoc
```

### Build Android App

```bash
# Debug build
./gradlew :android:assembleDebug

# Release build
./gradlew :android:assembleRelease

# Install on device
./gradlew :android:installDebug

# Run tests
./gradlew :android:test
```

### Build Desktop App

```bash
# Run application
./gradlew :desktop:run

# Build JAR
./gradlew :desktop:build

# Create native installer
./gradlew :desktop:jpackage

# Run tests
./gradlew :desktop:test
```

### Build Everything

```bash
# Clean and build all modules
./gradlew clean build

# Build with all tests
./gradlew build --info

# Generate reports
./gradlew test jacocoTestReport
```

---

## 📱 Specific Build Instructions

### Android

**Complete Guide:** See [android/README.md](android/README.md)

```bash
cd android

# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Build release APK
./gradlew assembleRelease
```

### Desktop

**Complete Guide:** See [desktop/README.md](desktop/README.md)

```bash
cd desktop

# Run application
./gradlew run

# Create Windows installer
./gradlew jpackage -Parch=x86_64

# Create macOS DMG
./gradlew jpackage -Parch=aarch64

# Create Linux DEB
./gradlew jpackage -Plinux-deb
```

---

## 📚 Documentation

Each module has comprehensive documentation:

- **[Core Module](core/README.md)** – Shared encryption and services
- **[Android App](android/README.md)** – Mobile application documentation
- **[Desktop App](desktop/README.md)** – Desktop application documentation

### Additional Resources

| Document | Purpose |
|----------|---------|
| Security Model | Encryption architecture and best practices |
| Architecture | MVVM patterns and module organization |
| API Reference | Core services and repositories |
| Testing Guide | Unit and UI testing |

---

## 🧪 Testing

### Run All Tests

```bash
# Test all modules
./gradlew test

# Test specific module
./gradlew :core:test
./gradlew :android:test
./gradlew :desktop:test

# Generate coverage report
./gradlew test jacocoTestReport
```

### Test Coverage Goals

- **Core Module**: ≥ 85% line coverage
- **Services**: ≥ 90% line coverage
- **Repositories**: ≥ 80% line coverage

---

## 🗺️ Roadmap

### Version 1.0 ✅ (Complete)

**Core Module:**
- ✅ AES-256-CBC encryption with PBKDF2
- ✅ SQLite database with DAO pattern
- ✅ Password generator
- ✅ Session management
- ✅ Secure credential storage

**Android App (v1.0.1):**
- ✅ Material Design 3 UI
- ✅ Biometric authentication
- ✅ Full credential management
- ✅ Secure notes & file vault
- ✅ Payment card management
- ✅ QR code scanning
- ✅ Dark mode support
- ✅ Card expiration alerts

**Desktop App (v1.0):**
- ✅ JavaFX cross-platform UI
- ✅ Session management with auto-lock
- ✅ Password generator
- ✅ Credential dashboard
- ✅ Local backup & restore
- ✅ Admin features

### Version 1.1 (Planned)

- [ ] Enhanced password analytics
- [ ] Password reuse detection
- [ ] Password age tracking
- [ ] Improved search capabilities
- [ ] Dark mode for desktop

### Version 1.5 (Planned)

- [ ] Google Drive cloud sync (encrypted)
- [ ] Password breach checker
- [ ] Multi-device synchronization
- [ ] Advanced security dashboard
- [ ] Backup versioning

### Version 2.0+ (Future)

- [ ] Firebase integration
- [ ] Firebase authentication
- [ ] Multi-user support
- [ ] Team/family sharing
- [ ] Browser extension integration
- [ ] Advanced analytics reports

---

## 🔐 Security Features

### Implemented

✅ **Encryption**
- AES-256-CBC with unique IVs per entry
- PBKDF2 key derivation (100,000 iterations)
- Secure random number generation

✅ **Authentication**
- Master password with SHA-256 + PBKDF2
- Biometric authentication (Android)
- Session management with auto-lock
- Activity monitoring

✅ **Privacy**
- Zero-knowledge architecture
- Local-first storage (no cloud required)
- Memory protection (clear sensitive data)
- Secure file deletion

### Future Enhancements

🔜 **Planned Security Features**
- [ ] Breach detection (HaveIBeenPwned API)
- [ ] Two-factor authentication
- [ ] Hardware token support (YubiKey)
- [ ] End-to-end encrypted cloud sync
- [ ] Advanced audit logging

---

## 🤝 Contributing

Contributions are welcome! Please follow these guidelines:

1. **Fork the repository**
2. **Create a feature branch** (`git checkout -b feature/amazing-feature`)
3. **Commit changes** (`git commit -m 'Add amazing feature'`)
4. **Push to branch** (`git push origin feature/amazing-feature`)
5. **Open a Pull Request**

### Code Guidelines

- Follow Java naming conventions
- Write unit tests for new features
- Update documentation
- Ensure all tests pass: `./gradlew build`

---

## 📄 License

This project is licensed under the **MIT License** - see [LICENSE](LICENSE) file for details.

---

## 🙏 Acknowledgments

### Libraries & Frameworks

- **Java Cryptography Architecture** – JDK built-in encryption
- **SQLite** – Lightweight local database
- **JavaFX** – Cross-platform desktop UI
- **AndroidX & Material Design 3** – Modern Android development
- **Gradle** – Build automation
- **ZXing** – QR code generation
- **JUnit & TestFX** – Testing frameworks

### Contributors

- **Abir Hasan Arko** – Lead Developer & Architect

---

## 📞 Support & Contact

### Getting Help

- 🐛 **Report Bugs**: [Open an issue](https://github.com/AbirHasanArko/PassMan/issues)
- 💡 **Request Features**: [GitHub Discussions](https://github.com/AbirHasanArko/PassMan/discussions)
- 💬 **Ask Questions**: [GitHub Discussions](https://github.com/AbirHasanArko/PassMan/discussions)

### Developer Contact

- **Name**: Abir Hasan Arko
- **GitHub**: [@AbirHasanArko](https://github.com/AbirHasanArko)
- **Email**: Contact via GitHub

---

## 🌟 Quick Start Commands

### For Android Developers

```bash
# Clone and setup
git clone https://github.com/AbirHasanArko/PassMan.git
cd PassMan/android

# Build and run
./gradlew assembleDebug
./gradlew installDebug

# Or open in Android Studio and click Run
```

### For Desktop Developers

```bash
# Clone and setup
git clone https://github.com/AbirHasanArko/PassMan.git
cd PassMan/desktop

# Build and run
./gradlew run

# Create installer
./gradlew jpackage
```

### For Full Project Build

```bash
# Clone and setup
git clone https://github.com/AbirHasanArko/PassMan.git
cd PassMan

# Build all modules
./gradlew clean build

# Run tests
./gradlew test
```

---

## 📊 Project Status

| Component | Status | Version |
|-----------|--------|---------|
| **Core Module** | ✅ Complete | 1.0 |
| **Android App** | ✅ Complete | 1.0.1 |
| **Desktop App** | ✅ Complete | 1.0 |
| **Tests** | ✅ Comprehensive | - |
| **Documentation** | ✅ Complete | - |

---

## 🚀 What's Next?

1. **Try the App**: Build and run on Android or Desktop
2. **Read Documentation**: Check module-specific READMEs
3. **Explore Code**: Understanding the architecture
4. **Contribute**: Submit issues or pull requests
5. **Share Feedback**: Help improve PassMan

---

**Built with ❤️ by [Abir Hasan Arko](https://github.com/AbirHasanArko)**

*Your credentials. Your control. Your security.*
