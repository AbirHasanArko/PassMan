# 🔐 PassMan – Desktop Application (JavaFX)

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.java.net/)
[![JavaFX](https://img.shields.io/badge/JavaFX-21-blue.svg)](https://openjfx.io/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Build](https://img.shields.io/badge/Build-Gradle-brightgreen.svg)](https://gradle.org/)

> A cross-platform desktop password manager built with JavaFX, featuring military-grade encryption, comprehensive credential management, secure notes, file vault, and advanced security analytics.

---

## 📋 Table of Contents

- [About](#about)
- [Features](#features)
- [Requirements](#requirements)
- [Getting Started](#getting-started)
- [Building & Running](#building--running)
- [Architecture](#architecture)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Security](#security)
- [Configuration](#configuration)
- [Deployment](#deployment)
- [Future Roadmap](#future-roadmap)
- [About the Developer](#about-the-developer)

---

## About

**PassMan Desktop** is a cross-platform password manager application built with JavaFX 21, delivering enterprise-grade security with a professional, user-friendly interface. The application runs on Windows, macOS, and Linux with native installers and supports advanced password management, analytics, and security features.

### Key Highlights

- **🔒 Military-Grade Encryption**: AES-256-CBC with PBKDF2 key derivation (100,000+ iterations)
- **🖥️ Cross-Platform**: Windows, macOS, and Linux support with native installers
- **🎨 Modern JavaFX UI**: Professional Material-inspired interface with FXML layouts
- **⚡ High Performance**: Optimized for speed with native Java compilation
- **🔐 Privacy-First**: All data stored locally with SQLite - no cloud required
- **🔑 Session Management**: Auto-lock with configurable timeout
- **📊 Advanced Analytics**: Password health analysis and security reports
- **🎯 Intuitive Interface**: Professional dashboard with organized credential management

---

## Features

### Authentication & Security

- 🔐 **Master Password Protection** – SHA-256 + PBKDF2 hashing
- 🔒 **Session Management** – Configurable timeout with auto-logout
- ⏱️ **Activity Monitoring** – Tracks user activity for security
- 🛡️ **Secure Memory Handling** – Sensitive data cleared after use
- 🔑 **Strong Encryption** – AES-256-CBC with unique IVs per entry

### Credential Management

- 🔐 **Full CRUD Operations** – Add, edit, view, delete credentials
- 🔍 **Advanced Search & Filtering** – Find credentials instantly
- 📋 **Credential Details** – Comprehensive credential information display
- 🎲 **Password Generator** – Customizable strength and length options
- ⭐ **Quick Copy** – One-click clipboard copy with auto-clear
- 📊 **Credential Summary** – Overview of all stored credentials

### Data Organization

- 📝 **Secure Notes** – Encrypted note storage and management
- 💳 **Card Management** – Store and manage payment card information
- 🗂️ **File Vault** – Encrypted file storage with organization
- 📄 **Document Storage** – Secure document and attachment storage
- 🏷️ **Categories** – Organize credentials by category

### Security Analytics

- 📈 **Password Strength Analysis** – Real-time strength assessment
- 🔄 **Reuse Detection** – Identify duplicate passwords
- ⏰ **Age Tracking** – Monitor password age with indicators
- 📊 **Security Dashboard** – Visual security metrics and insights
- 🎯 **Recommendations** – Actionable security suggestions
- 🕸️ **Dependency Analysis** – Visualize password relationships

### Advanced Features

- 📸 **QR Code Generation** – Generate QR codes for credential sharing
- 🔐 **Secure Sharing** – Time-limited QR sharing
- 💾 **Backup & Restore** – Local backup creation and restoration
- 📊 **Reports** – Security reports and statistics
- ⚙️ **Settings** – Customizable application preferences
- 🎨 **Theming** – Multiple theme options (Light/Dark)

### Administrative Features

- 👤 **User Management** – Manage application users
- 📊 **Admin Dashboard** – Administrative controls and monitoring
- 🔧 **Configuration** – System-wide configuration options
- 📈 **Audit Logging** – Track security events
- 🏆 **Quiz System** – Educational security quizzes
- 🎮 **Gamification** – Achievement badges and missions

---

## Requirements

### System Requirements

- **Operating System**: Windows 10+, macOS 10.12+, or Linux (Ubuntu 18.04+)
- **RAM**: 4 GB minimum (8 GB recommended)
- **Storage**: 200 MB free space
- **Java**: JDK 17 or later (bundled in installers)
- **JavaFX**: 21.0+ (bundled in application)

### For Development

- **Java Development Kit (JDK)**: 17 or later
- **Gradle**: 8.0+ (wrapper included)
- **IDE**: IntelliJ IDEA, Eclipse, or VS Code (recommended: IntelliJ IDEA)
- **Git**: For cloning repository

---

## Getting Started

### Clone Repository

```bash
git clone https://github.com/AbirHasanArko/PassMan.git
cd PassMan/desktop
```

### Install from Executable

**Windows:**
```bash
# Download and run PassMan-1.0.0.exe installer
# Or use: msiexec /i PassMan-1.0.0.msi
```

**macOS:**
```bash
# Download and open PassMan-1.0.0.dmg
# Or use: brew install passman (if available)
```

**Linux:**
```bash
# Ubuntu/Debian
sudo dpkg -i passman-1.0.0.deb

# Or use your package manager
sudo apt install ./passman-1.0.0.deb
```

---

## Building & Running

### Run Directly

```bash
# Run with Gradle
./gradlew run

# Run with arguments
./gradlew run --args="--debug"
```

### Build Application

```bash
# Build JAR file
./gradlew build

# Build with distribution
./gradlew installDist

# Run from distribution
./build/install/PassMan/bin/PassMan
```

### Create Native Installer

```bash
# Windows (EXE/MSI)
./gradlew jpackage -Parch=x86_64 -Pmsibundle

# macOS (DMG)
./gradlew jpackage -Parch=aarch64

# Linux (DEB)
./gradlew jpackage -Plinux-deb

# Output: build/jpackage/
```

### Run Tests

```bash
# Run all tests
./gradlew test

# Run with coverage
./gradlew test jacocoTestReport

# Run specific test class
./gradlew test --tests="com.passman.desktop.*"
```

---

## Architecture

PassMan Desktop follows the **MVVM (Model-View-ViewModel)** architecture pattern:

```
┌──────────────────────────────────────────┐
│          View Layer (FXML)               │
│  ┌──────────────────────────────────┐   │
│  │  FXML Files + CSS                │   │
│  │  (login.fxml, dashboard.fxml)    │   │
│  └──────────┬───────────────────────┘   │
└─────────────┼──────────────────────────┘
              │
┌─────────────▼──────────────────────────┐
│     Controller Layer (JavaFX)          │
│  ┌──────────────────────────────────┐  │
│  │  LoginController                 │  │
│  │  DashboardController             │  │
│  │  (Handle user interactions)      │  │
│  └──────────┬───────────────────────┘  │
└─────────────┼──────────────────────────┘
              │
┌─────────────▼──────────────────────────┐
│    ViewModel Layer (Business Logic)    │
│  ┌──────────────────────────────────┐  │
│  │  LoginViewModel                  │  │
│  │  DashboardViewModel              │  │
│  │  (Manage UI state & logic)       │  │
│  └──────────┬───────────────────────┘  │
└─────────────┼──────────────────────────┘
              │
┌─────────────▼──────────────────────────┐
│    Service Layer (Core Module)         │
│  ┌──────────────────────────────────┐  │
│  │  EncryptionService               │  │
│  │  PasswordAnalysisService         │  │
│  │  BackupService                   │  │
│  └──────────┬───────────────────────┘  │
└─────────────┼──────────────────────────┘
              │
┌─────────────▼──────────────────────────┐
│      Data Layer (SQLite Database)      │
│  ┌──────────────────────────────────┐  │
│  │  CredentialRepository            │  │
│  │  SQLite Local Database           │  │
│  └──────────────────────────────────┘  │
└──────────────────────────────────────────┘
```

### Design Patterns

- **MVVM**: Separation of UI from business logic
- **Repository Pattern**: Abstract data access
- **Singleton**: SceneManager, SessionManager, DatabaseManager
- **Observer**: JavaFX Bindings for reactive UI updates
- **Factory**: SceneManager for creating scenes
- **Strategy**: Different password generation strategies

### Scene Navigation

```
┌─────────────┐
│   Splash    │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│   Login     │
└──────┬──────┘
       │
       ▼
┌──────────────────────────────────────────┐
│          Main Dashboard                  │
│  ├─ Credential Management               │
│  ├─ Secure Notes                        │
│  ├─ File Vault                          │
│  ├─ Password Generator                  │
│  ├─ Security Analytics                  │
│  ├─ Admin Dashboard                     │
│  ├─ Settings                            │
│  └─ Help/About                          │
└──────────────────────────────────────────┘
```

---

## Technology Stack

### Core Framework
- **Language**: Java 17 (with preview features)
- **Framework**: JavaFX 21.0+
- **Build Tool**: Gradle 8.5 (Kotlin DSL)

### UI & Graphics
- **JavaFX Modules**:
  - `javafx.controls` – UI components
  - `javafx.fxml` – XML-based UI definitions
  - `javafx.graphics` – Graphics rendering
  - `javafx.base` – Core classes
  - `javafx.web` – Web view component
  - `javafx.swing` – Swing interoperability
- **Layout**: FXML with CSS styling
- **Scene Management**: Custom SceneManager

### Database & Persistence
- **Database**: SQLite
- **Database Library**: org.xerial:sqlite-jdbc:3.43.0.0
- **Repository Pattern**: For data abstraction

### Security & Cryptography
- **Encryption**: Java Cryptography Architecture (JCA)
- **Algorithm**: AES-256-CBC with PBKDF2
- **QR Code**: com.google.zxing:core & javase 3.5.2

### Additional Libraries
- **Date/Time**: threeten-extra 1.7.2 (additional time utilities)
- **Testing**: 
  - JUnit 5.10.1 (unit testing)
  - TestFX 4.0.18 (UI testing)

### Deployment
- **Packaging**: jpackage (Java 17+)
- **Runtime Builder**: beryx/runtime plugin 1.13.1
- **JVM Options**: Custom memory allocation and module exports

---

## Project Structure

```
PassMan/desktop/
├── src/main/java/com/passman/desktop/
│   ├── MainApp.java                     # Application entry point
│   ├── Launcher.java                    # Application launcher
│   ├── SessionManager.java              # Session management
│   ├── SceneManager.java                # Scene navigation
│   ├── DialogUtils.java                 # Dialog utilities
│   │
│   ├── ui/                              # UI Screens & Controllers
│   │   ├── login/
│   │   │   ├── LoginController.java     # Login screen controller
│   │   │   └── LoginViewModel.java      # Login business logic
│   │   ├── dashboard/
│   │   │   ├── DashboardController.java
│   │   │   ├── DashboardViewModel.java
│   │   │   └── PasswordTableViewController.java
│   │   ├── credential/
│   │   │   ├── CredentialController.java
│   │   │   └── CredentialViewModel.java
│   │   ├── notes/
│   │   │   ├── NotesController.java
│   │   │   └── NotesViewModel.java
│   │   ├── vault/
│   │   │   ├── FileVaultController.java
│   │   │   └── FileVaultViewModel.java
│   │   ├── generator/
│   │   │   ├── PasswordGeneratorController.java
│   │   │   └── PasswordGeneratorViewModel.java
│   │   ├── analytics/
│   │   │   ├── AnalyticsController.java
│   │   │   └── AnalyticsViewModel.java
│   │   ├── backup/
│   │   │   ├── BackupController.java
│   │   │   └── BackupViewModel.java
│   │   ├── qr/
│   │   │   ├── QRGeneratorController.java
│   │   │   └── QRGeneratorViewModel.java
│   │   ├── admin/
│   │   │   ├── AdminDashboardController.java
│   │   │   └── AdminViewModel.java
│   │   ├── identity/
│   │   │   └── Identity-related screens
│   │   ├── graph/
│   │   │   └── Graph visualization screens
│   │   ├── quiz/
│   │   │   └── Security quiz screens
│   │   └── placeholder/
│   │       └── Placeholder components
│   │
│   ├── utils/                           # Utility classes
│   │   ├── SessionTimeoutMonitor.java
│   │   └── Other utilities
│   │
│   └── resources/ (see below)
│
├── src/main/resources/
│   ├── fxml/                            # FXML Layout Files
│   │   ├── login.fxml
│   │   ├── dashboard.fxml
│   │   ├── credential.fxml
│   │   ├── generator.fxml
│   │   ├── analytics.fxml
│   │   ├── settings.fxml
│   │   └── Other screen FXMLs
│   ├── styles/                          # CSS Stylesheets
│   │   ├── style.css
│   │   ├── light-theme.css
│   │   ├── dark-theme.css
│   │   └── components.css
│   └── icons/                           # Application Icons & Images
│       ├── app-icon.png
│       ├── passman-logo.png
│       └── Various UI icons
│
├── src/test/java/                       # Unit Tests
│   └── com/passman/desktop/
│       ├── LoginControllerTest.java
│       └── Other test classes
│
├── build.gradle.kts                     # Gradle build configuration
├── gradle/
│   └── wrapper/                         # Gradle wrapper
├── gradlew                              # Gradle wrapper (Unix/Linux/macOS)
├── gradlew.bat                          # Gradle wrapper (Windows)
└── README.md                            # This file
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
AES-256-CBC Encryption (Unique IV per entry)
```

### Security Features

1. **Zero-Knowledge Design**: Master password never stored in plaintext
2. **Per-Entry Encryption**: Each credential encrypted with unique IV
3. **Memory Protection**: Sensitive data cleared from memory immediately
4. **Secure Random**: Cryptographically strong random number generation
5. **Session Timeout**: Automatic logout after inactivity
6. **Activity Monitoring**: Tracks user activity for security events

### Best Practices

- Master password hashed with salt (SHA-256 + PBKDF2 100,000 iterations)
- All data encrypted before storage
- Session keys derived from master password
- Automatic cleanup on application exit
- Secure temporary file handling
- Input validation on all entries

---

## Configuration

### Application Settings

Default configuration is stored in `~/.passman/config.properties`:

```properties
# Session Management
session.timeout.minutes=15
auto.lock.enabled=true

# Encryption
pbkdf2.iterations=100000
aes.key.size=256

# Backup
backup.auto.enabled=true
backup.interval.hours=24
backup.max.versions=10

# UI
theme=light
language=en_US
window.width=1200
window.height=800

# Database
database.path=~/.passman/passman.db
database.pool.size=5
```

### Database Location

- **Windows**: `%APPDATA%\PassMan\passman.db`
- **macOS**: `~/Library/Application Support/PassMan/passman.db`
- **Linux**: `~/.local/share/PassMan/passman.db`

---

## Deployment

### Building Release Package

```bash
# Create native installer for current platform
./gradlew jpackage

# Specify target platform
./gradlew jpackage -Pjpackage.platform=windows
./gradlew jpackage -Pjpackage.platform=macos
./gradlew jpackage -Pjpackage.platform=linux
```

### Platform-Specific Outputs

**Windows:**
- `build/jpackage/PassMan-1.0.0.exe` (EXE installer)
- `build/jpackage/PassMan-1.0.0.msi` (MSI installer)

**macOS:**
- `build/jpackage/PassMan-1.0.0.dmg` (DMG image)
- Built-in code signing support

**Linux:**
- `build/jpackage/passman-1.0.0.deb` (Debian package)
- `build/jpackage/passman-1.0.0.rpm` (RPM package)

### Distribution

1. Sign installers (recommended for production)
2. Create release on GitHub
3. Upload installers to release
4. Update download links

---

## Future Roadmap

### Version 1.0 (Current) ✅
- ✅ Core password management
- ✅ AES-256 encryption
- ✅ Master password authentication
- ✅ MVVM architecture
- ✅ Session management with auto-lock
- ✅ Password generator
- ✅ Secure notes
- ✅ File vault
- ✅ Local backup/restore
- ✅ Professional JavaFX UI

### Version 1.1 (Planned)
- [ ] Dark mode theme
- [ ] Enhanced password analytics
- [ ] Password reuse detection
- [ ] Password age tracking
- [ ] Improved QR code sharing

### Version 1.5 (Planned)
- [ ] Google Drive cloud sync (encrypted)
- [ ] Password breach checker
- [ ] Advanced security dashboard
- [ ] Custom password policies
- [ ] Improved backup management

### Version 2.0+ (Future)
- [ ] Multi-user support
- [ ] Team/family sharing
- [ ] Browser extension integration
- [ ] Mobile app sync
- [ ] Advanced analytics reports
- [ ] Hardware token support

---

## About the Developer

**Developer**: Abir Hasan Arko

PassMan Desktop provides a secure, professional password management solution for desktop users with enterprise-grade encryption and a user-friendly interface.

---

## License

This project is licensed under the MIT License. See [LICENSE](../LICENSE) file for details.

---

## Support & Feedback

For bug reports, feature requests, or feedback:
- Open an issue on [GitHub](https://github.com/AbirHasanArko/PassMan/issues)
- Check out discussions on [GitHub Discussions](https://github.com/AbirHasanArko/PassMan/discussions)

---

**Built with ❤️ by Abir Hasan Arko**
