# 🔐 PassMan: All-in-One Security Solution

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.java.net/)
[![JavaFX](https://img.shields.io/badge/JavaFX-20+-blue.svg)](https://openjfx.io/)
[![Android](https://img.shields.io/badge/Android-SDK-green.svg)](https://developer.android.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Build](https://img.shields.io/badge/Build-Gradle-brightgreen.svg)](https://gradle.org/)

> **A secure, feature-rich digital security manager with desktop (JavaFX) and Android support, featuring military-grade encryption, secure file storage and sharing, cloud sync, gamification, and advanced security analytics.**

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Key Features](#-key-features)
- [Architecture](#-architecture)
- [Technology Stack](#-technology-stack)
- [Project Structure](#-project-structure)
- [Security Model](#-security-model)
- [Getting Started](#-getting-started)
- [Building & Running](#-building--running)
- [Configuration](#-configuration)
- [API Documentation](#-api-documentation)
- [Testing](#-testing)
- [Deployment](#-deployment)
- [Contributing](#-contributing)
- [Roadmap](#-roadmap)

---

## 🎯 Overview

**PassMan** is an enterprise-grade password management solution designed for both desktop (Windows/macOS/Linux) and Android platforms. Built with security-first principles, it provides a comprehensive suite of features including encrypted credential storage, secure cloud backups, password health analytics, gamified security education, and administrative controls.

### Why PassMan?

- **🔒 Military-Grade Security**: AES-256-CBC encryption with PBKDF2 key derivation (100,000+ iterations)
- **📱 Cross-Platform**:  Shared core logic between Desktop (JavaFX) and Android via multi-module architecture
- **☁️ Cloud Sync**:  Encrypted backups to Google Drive with zero-knowledge architecture
- **📊 Password Intelligence**: Reuse detection, age tracking, and dependency graph analysis
- **🎮 Gamification**: Security missions, quizzes, leaderboards to promote best practices
- **🔄 QR Sharing**: Time-limited, ephemeral secure credential sharing via QR codes
- **👥 Multi-User Support**: Role-based access control (RBAC) with admin privileges
- **🚀 Performance**: Non-blocking UI with advanced concurrency and in-memory caching

---

## ✨ Key Features

### Core Password Management
- ✅ **Master Password Authentication** with salted hash (SHA-256 + PBKDF2)
- ✅ **Per-Entry Encryption** using AES-256-CBC with unique IVs
- ✅ **SQLite Local Database** with transaction-safe CRUD operations
- ✅ **Advanced Search & Filtering** with real-time in-memory cache
- ✅ **Secure Password Generator** with configurable complexity (multithreaded)
- ✅ **Auto-lock** and session timeout protection

### Security Analytics
- 📈 **Password Reuse Detection** using hashed signatures (no plaintext comparison)
- ⏰ **Password Age Tracking** with color-coded health indicators (green/yellow/red)
- 🕸️ **Dependency Graph Visualization** showing account relationships and similarity metrics
- 🔍 **Breach Detection** (future:  integration with HaveIBeenPwned API)
- 📊 **Security Score Dashboard** with actionable recommendations

### Backup & Sync
- 💾 **Local Encrypted Backups** (. pmbak format with SHA-256 checksums)
- ☁️ **Google Drive Integration** via OAuth2 (never uploads plaintext)
- 🗂️ **Multi-Segment Vault** (credentials/notes/metadata encrypted separately)
- ⏮️ **Point-in-Time Recovery** with timestamped backup versions
- 🔄 **Automatic Backup Scheduling** with configurable intervals

### Secure Sharing
- 📱 **QR Code Sharing** with ephemeral encryption keys (30-second expiration)
- 🔐 **Time-Limited Access** with automatic credential revocation
- 🌐 **Encrypted Share Links** for secure cross-device transfers

### Gamification & Education
- 🎮 **Security Missions** with progressive difficulty levels
- 🏆 **Achievement Badges** (Vault Beginner, Password Pro, Security Master, etc.)
- 📝 **Interactive Quizzes** on password security best practices
- 📊 **Global Leaderboards** synced via Firebase Firestore
- 📰 **Security Newsletter** (admin-published tips and updates)

### Administration
- 👤 **Role-Based Access Control**:  `game_setter`, `newsletter_publisher`, `super_admin`
- 📊 **Admin Dashboard** for user analytics and system health
- 🔧 **Configuration Management** for organization-wide policies
- 📈 **Audit Logging** for compliance and security monitoring

---

## 🏗️ Architecture

PassMan follows a **multi-module, layered architecture** optimized for code reuse between Desktop and Android:

```
┌─────────────────────────────────────────────────────────┐
│                    Presentation Layer                    │
│  ┌─────────────────────┐  ┌─────────────────────────┐  │
│  │  Desktop (JavaFX)   │  │   Android (Activities)  │  │
│  │  - FXML Views       │  │   - XML Layouts         │  │
│  │  - Controllers      │  │   - ViewModels          │  │
│  │  - ViewModels       │  │   - LiveData            │  │
│  └─────────┬───────────┘  └───────────┬─────────────┘  │
└────────────┼──────────────────────────┼─────────────────┘
             │                          │
             └──────────┬───────────────┘
                        │
┌───────────────────────▼─────────────────────────────────┐
│                    Core Module (Java)                    │
│  ┌─────────────────────────────────────────────────┐   │
│  │              Service Layer                       │   │
│  │  - EncryptionService                            │   │
│  │  - PasswordAnalysisService                      │   │
│  │  - BackupService                                │   │
│  │  - CloudSyncService (Google Drive)              │   │
│  │  - GamificationService                          │   │
│  └───────────────────┬─────────────────────────────┘   │
│                      │                                   │
│  ┌───────────────────▼─────────────────────────────┐   │
│  │           Repository Layer                       │   │
│  │  - CredentialRepository                         │   │
│  │  - BackupRepository                             │   │
│  │  - AdminRepository                              │   │
│  └───────────────────┬─────────────────────────────┘   │
│                      │                                   │
│  ┌───────────────────▼─────────────────────────────┐   │
│  │              DAO Layer                           │   │
│  │  - CredentialDAO (CRUD)                         │   │
│  │  - UserDAO                                      │   │
│  │  - AuditDAO                                     │   │
│  └───────────────────┬─────────────────────────────┘   │
│                      │                                   │
│  ┌───────────────────▼─────────────────────────────┐   │
│  │           Data Layer (SQLite)                    │   │
│  │  - Connection Pool                              │   │
│  │  - Transaction Manager                          │   │
│  │  - Migration Scripts                            │   │
│  └─────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
```

### Design Patterns Implemented

| Pattern | Usage | Location |
|---------|-------|----------|
| **MVVM** | UI data binding & separation of concerns | `desktop/viewmodels`, `android/viewmodels` |
| **Repository** | Abstract data source access | `core/repository` |
| **DAO** | Database operations encapsulation | `core/db/dao` |
| **Factory** | Cipher/Key derivation instance creation | `core/crypto/CipherFactory` |
| **Strategy** | Password generation algorithms | `core/generator/strategies` |
| **Observer** | Event-driven cross-module communication | `core/events/EventBus` |
| **Singleton** | Database connection, config manager | `core/db/DatabaseManager` |
| **Command** | Undoable operations (delete/update) | `core/commands` |
| **Decorator** | Encryption layer over backup strategies | `core/backup/decorators` |

### Concurrency Model

- **JavaFX**:  `Task<V>` and `Service<V>` for background operations
- **Android**: Coroutines + LiveData/Flow for reactive updates
- **Core**: `ExecutorService` (fixed thread pool, configurable size)
- **Async Composition**: `CompletableFuture` chains for complex workflows
- **Thread Safety**: Concurrent collections, `ReadWriteLock` for cache access

---

## 🛠️ Technology Stack

### Desktop (JavaFX)
- **Java**:  17 LTS (OpenJDK)
- **JavaFX**: 20.0.1
- **UI**:  FXML + CSS styling
- **Charts**: JavaFX Charts API for analytics

### Android
- **Minimum SDK**: 26 (Android 8.0 Oreo)
- **Target SDK**: 34 (Android 14)
- **Architecture Components**: ViewModel, LiveData, Room (optional wrapper over SQLite)
- **Dependency Injection**: Hilt/Dagger (optional)

### Core Library
- **Language**: Java 17 (compatible with Android via desugaring)
- **Database**: SQLite 3.41+ (`org.xerial:sqlite-jdbc:3.43.0.0`)
- **Cryptography**: Java Cryptography Architecture (JCA) + BouncyCastle 1.70
- **JSON**: Jackson 2.15 / Gson 2.10
- **Logging**: SLF4J + Logback

### Cloud & Integration
- **Google Drive API**: `com.google.apis:google-api-services-drive:v3-rev20220815-2.0.0`
- **OAuth2**: `com.google.auth:google-auth-library-oauth2-http:1.19.0`
- **Firebase**:  Firestore SDK for leaderboard sync
- **QR Code**: ZXing (`com.google.zxing:core:3.5.1`)

### Build & Testing
- **Build System**: Gradle 8.5 (Kotlin DSL)
- **Testing**: JUnit 5, Mockito 5, AssertJ, Testcontainers (for integration tests)
- **Code Coverage**: JaCoCo
- **Static Analysis**: SpotBugs, Checkstyle, PMD

---

## 📁 Project Structure

```
PassMan/
├── core/                           # Shared business logic (pure Java)
│   ├── src/main/java/com/passman/core/
│   │   ├── crypto/                 # Encryption, key derivation
│   │   │   ├── AESCipher.java
│   │   │   ├── PBKDF2KeyDerivation.java
│   │   │   ├── CipherFactory.java
│   │   │   └── SecureRandomProvider.java
│   │   ├── db/                     # Database layer
│   │   │   ├── DatabaseManager.java
│   │   │   ├── TransactionManager.java
│   │   │   ├── dao/
│   │   │   │   ├── CredentialDAO.java
│   │   │   │   ├── UserDAO.java
│   │   │   │   ├── AdminDAO.java
│   │   │   │   └── AuditDAO.java
│   │   │   └── migrations/
│   │   │       ├── V1__InitialSchema.sql
│   │   │       └── V2__AddPasswordAge.sql
│   │   ├── models/                 # Domain entities
│   │   │   ├── Credential.java
│   │   │   ├── User.java
│   │   │   ├── Backup.java
│   │   │   ├── Mission.java
│   │   │   └── Admin.java
│   │   ├── repository/             # Data access abstraction
│   │   │   ├── CredentialRepository. java
│   │   │   ├── BackupRepository.java
│   │   │   └── AdminRepository.java
│   │   ├── services/               # Business logic
│   │   │   ├── EncryptionService.java
│   │   │   ├── PasswordAnalysisService.java
│   │   │   ├── BackupService.java
│   │   │   ├── CloudSyncService.java
│   │   │   ├── QRSharingService.java
│   │   │   ├── GamificationService.java
│   │   │   └── AdminService.java
│   │   ├── generator/              # Password generation
│   │   │   ├── PasswordGenerator.java
│   │   │   └── strategies/
│   │   │       ├── AlphanumericStrategy.java
│   │   │       └── SymbolStrategy.java
│   │   ├── analysis/               # Password analytics
│   │   │   ├── ReuseDetector.java
│   │   │   ├── AgeTracker.java
│   │   │   └── DependencyGraph.java
│   │   ├── concurrency/            # Threading utilities
│   │   │   ├── TaskExecutor.java
│   │   │   └── AsyncResult.java
│   │   ├── events/                 # Event bus
│   │   │   ├── EventBus.java
│   │   │   └── events/
│   │   └── utils/                  # Helpers
│   │       ├── HashUtil.java
│   │       └── ValidationUtil.java
│   └── src/test/java/              # Unit tests
│       └── com/passman/core/
│
├── desktop/                        # JavaFX application
│   ├── src/main/java/com/passman/desktop/
│   │   ├── PassManApp.java         # Main application entry
│   │   ├── controllers/            # FXML controllers
│   │   │   ├── LoginController.java
│   │   │   ├── MainDashboardController.java
│   │   │   ├── CredentialListController.java
│   │   │   ├── AddEditCredentialController.java
│   │   │   ├── PasswordGeneratorController.java
│   │   │   ├── SettingsController.java
│   │   │   ├── BackupController.java
│   │   │   ├── AnalyticsController.java
│   │   │   ├── QRSharingController.java
│   │   │   ├── GamificationController.java
│   │   │   └── AdminDashboardController.java
│   │   ├── viewmodels/             # MVVM ViewModels
│   │   │   ├── CredentialViewModel.java
│   │   │   ├── AnalyticsViewModel.java
│   │   │   └── GamificationViewModel.java
│   │   ├── views/                  # FXML files
│   │   │   ├── login. fxml
│   │   │   ├── main-dashboard. fxml
│   │   │   ├── credential-list. fxml
│   │   │   └── analytics.fxml
│   │   ├── utils/                  # UI utilities
│   │   │   ├── AlertHelper.java
│   │   │   └── FXMLLoader.java
│   │   └── resources/
│   │       ├── css/
│   │       │   └── styles.css
│   │       ├── images/
│   │       └── fonts/
│   └── src/test/java/              # UI tests
│
├── android/                        # Android application
│   ├── app/
│   │   ├── src/main/java/com/passman/android/
│   │   │   ├── MainActivity. java
│   │   │   ├── ui/
│   │   │   │   ├── login/
│   │   │   │   ├── credentials/
│   │   │   │   ├── generator/
│   │   │   │   └── settings/
│   │   │   ├── viewmodels/
│   │   │   ├── adapters/
│   │   │   └── utils/
│   │   ├── src/main/res/
│   │   │   ├── layout/
│   │   │   ├── values/
│   │   │   └── drawable/
│   │   └── build.gradle. kts
│   └── build.gradle.kts
│
├── cli-tools/                      # Command-line utilities
│   └── src/main/java/com/passman/cli/
│       ├── BackupTool.java
│       ├── MigrationTool.java
│       └── ExportTool.java
│
├── docs/                           # Documentation
│   ├── API.md
│   ├── SECURITY.md
│   ├── ARCHITECTURE.md
│   └── USER_GUIDE.md
│
├── gradle/                         # Gradle wrapper
├── build.gradle.kts                # Root build configuration
├── settings.gradle.kts             # Module declarations
├── gradlew                         # Gradle wrapper script (Unix)
├── gradlew.bat                     # Gradle wrapper script (Windows)
├── . gitignore
├── LICENSE
└── README.md
```

---

## 🔒 Security Model

### Encryption Architecture

```
User Master Password
         │
         ▼
  PBKDF2 (100,000 iterations, SHA-256)
         │
         ▼
  Master Key (256-bit)
         │
         ├─────────────────────┬─────────────────────┐
         ▼                     ▼                     ▼
   Credentials           Secure Notes           Metadata
   (AES-256-CBC)        (AES-256-CBC)       (AES-256-CBC)
   + Unique IV          + Unique IV         + Unique IV
```

### Key Features

1. **Zero-Knowledge Architecture**: Master password never stored; only salted hash
2. **Per-Entry IVs**: Each encrypted field uses a unique initialization vector
3. **Memory Protection**: Sensitive data cleared from memory after use (`Arrays.fill()`)
4. **Secure Random**:  Cryptographically strong PRNG (SHA1PRNG/NativePRNG)
5. **Backup Integrity**: SHA-256 checksums verify backup file authenticity
6. **Cloud Encryption**: Data encrypted locally before cloud upload (Google Drive never sees plaintext)

### Authentication Flow

```
1. User enters master password
2. Generate hash:  PBKDF2(password, stored_salt, 100000, SHA256)
3. Compare with stored hash
4. On match:  Derive encryption key for session
5. Lock after 15 minutes of inactivity (configurable)
```

### Password Reuse Detection

```
1. Hash each password with BLAKE2 (fast, no decryption needed)
2. Store hash signature in reuse_signatures table
3. On new entry: check if hash exists
4. Display reuse warnings with affected accounts
```

---

## 🚀 Getting Started

### Prerequisites

- **Java Development Kit (JDK)**: 17 or later
- **Gradle**: 8.0+ (or use included wrapper)
- **Android Studio**: Arctic Fox+ (for Android development)
- **Android SDK**: API 26+ installed
- **Git**: For version control

### Clone Repository

```bash
git clone https://github.com/AbirHasanArko/PassMan.git
cd PassMan
```

### Initial Setup

1. **Configure Environment Variables**

```bash
# Create .env file (never commit this!)
cp .env.example .env

# Edit with your credentials
nano .env
```

```properties
# . env content
GOOGLE_DRIVE_CLIENT_ID=your_client_id_here
GOOGLE_DRIVE_CLIENT_SECRET=your_client_secret_here
FIREBASE_PROJECT_ID=your_firebase_project
FIREBASE_API_KEY=your_firebase_api_key
```

2. **Database Initialization**

On first run, PassMan automatically creates the SQLite database at:
- **Windows**: `%APPDATA%/PassMan/passman.db`
- **macOS**: `~/Library/Application Support/PassMan/passman.db`
- **Linux**: `~/.local/share/PassMan/passman.db`

3. **Google Drive Setup** (Optional)

Follow [docs/GOOGLE_DRIVE_SETUP. md](docs/GOOGLE_DRIVE_SETUP.md) to: 
- Create OAuth2 credentials in Google Cloud Console
- Enable Google Drive API
- Configure redirect URIs

---

## 🔨 Building & Running

### Desktop Application

```bash
# Build core module
./gradlew : core:build

# Build desktop application
./gradlew : desktop:build

# Run desktop application
./gradlew :desktop:run

# Create distributable package
./gradlew :desktop:jpackage
# Output: desktop/build/jpackage/PassMan-1.0.0.{exe|dmg|deb}
```

#### Running in IntelliJ IDEA

When running the desktop application from IntelliJ IDEA:

1. **Reimport Gradle Project**: After cloning or updating dependencies, go to:
   - `File` → `Invalidate Caches / Restart` → `Invalidate and Restart`
   - Or right-click on `build.gradle.kts` → `Reload Gradle Project`

2. **Use Gradle Run Task**: In the Gradle tool window (View → Tool Windows → Gradle):
   - Navigate to `PassMan → desktop → Tasks → application → run`
   - Double-click to run the application

3. **Alternative - Delegate to Gradle**: Configure IntelliJ to use Gradle for running:
   - `File` → `Settings` → `Build, Execution, Deployment` → `Build Tools` → `Gradle`
   - Under "Build and run using" select: `Gradle (Default)`
   - Under "Run tests using" select: `Gradle (Default)`

> **Note**: If you see `ClassNotFoundException: javafx.scene.text.Font`, this means IntelliJ's run configuration is not properly configured for JavaFX modules. Follow steps 1-3 above to resolve.

```

### Android Application

```bash
# Build debug APK
./gradlew : android:app:assembleDebug

# Install on connected device
./gradlew :android:app:installDebug

# Build release APK (requires keystore configuration)
./gradlew :android:app:assembleRelease
```

### CLI Tools

```bash
# Run backup tool
./gradlew :cli-tools: run --args="backup --output /path/to/backup.pmbak"

# Run database migration
./gradlew :cli-tools:run --args="migrate --version 2"
```

---

## ⚙️ Configuration

### Application Settings

Edit `desktop/src/main/resources/config. properties`:

```properties
# Session Management
session.timeout. minutes=15
auto.lock.enabled=true

# Encryption
pbkdf2.iterations=100000
aes.key.size=256

# Backup
backup.auto.enabled=true
backup.interval.hours=24
backup.max.versions=10

# Cloud Sync
cloud. sync.enabled=false
cloud.provider=google_drive

# UI
theme=light
font.size=14
language=en_US

# Concurrency
thread.pool.size=4
```

### Admin Configuration

Manage admin roles via CLI or desktop admin panel:

```bash
# Promote user to admin
java -jar cli-tools. jar admin promote --username john_doe --role super_admin

# List all admins
java -jar cli-tools.jar admin list
```

---

## 📚 API Documentation

### Core Services

#### EncryptionService

```java
public interface EncryptionService {
    /**
     * Encrypts plaintext using AES-256-CBC with a unique IV. 
     * @param plaintext Data to encrypt
     * @param masterKey Derived encryption key
     * @return Base64-encoded ciphertext with prepended IV
     */
    String encrypt(String plaintext, SecretKey masterKey) throws EncryptionException;
    
    /**
     * Decrypts ciphertext. 
     * @param ciphertext Base64-encoded encrypted data
     * @param masterKey Derived decryption key
     * @return Plaintext string
     */
    String decrypt(String ciphertext, SecretKey masterKey) throws DecryptionException;
}
```

#### PasswordAnalysisService

```java
public interface PasswordAnalysisService {
    /**
     * Detects password reuse across all credentials.
     * @return Map of password hash to list of credential IDs using it
     */
    Map<String, List<Long>> detectReuse();
    
    /**
     * Calculates password age in days.
     * @param credentialId ID of credential to check
     * @return Age in days
     */
    long calculateAge(long credentialId);
    
    /**
     * Generates dependency graph showing password similarity.
     * @return Graph with nodes (credentials) and weighted edges (similarity 0-1)
     */
    DependencyGraph buildDependencyGraph();
}
```

#### CloudSyncService

```java
public interface CloudSyncService {
    /**
     * Uploads encrypted backup to Google Drive.
     * @param backupFile Local backup file
     * @return CompletableFuture with remote file ID
     */
    CompletableFuture<String> uploadBackup(File backupFile);
    
    /**
     * Downloads and decrypts backup from cloud.
     * @param fileId Remote file identifier
     * @return CompletableFuture with local file path
     */
    CompletableFuture<File> downloadBackup(String fileId);
}
```

See [docs/API.md](docs/API.md) for complete API reference.

---

## 🧪 Testing

### Run All Tests

```bash
# Run all unit tests
./gradlew test

# Run with coverage report
./gradlew test jacocoTestReport

# View coverage:  open core/build/reports/jacoco/test/html/index.html
```

### Test Structure

```
core/src/test/java/
├── crypto/
│   ├── AESCipherTest.java          # Encryption/decryption tests
│   └── PBKDF2KeyDerivationTest.java
├── services/
│   ├── EncryptionServiceTest.java
│   ├── PasswordAnalysisServiceTest.java
│   └── BackupServiceTest.java
└── repository/
    └── CredentialRepositoryTest. java
```

### Coverage Goals

- **Core Module**: ≥ 85% line coverage
- **Services**: ≥ 90% line coverage
- **DAOs**: ≥ 80% line coverage

---

## 📦 Deployment

### Desktop Distribution

**Windows (EXE + Installer):**
```bash
./gradlew :desktop:jpackage --type exe
# Output: desktop/build/jpackage/PassMan-1.0.0.exe
```

**macOS (DMG):**
```bash
./gradlew :desktop:jpackage --type dmg
# Output: desktop/build/jpackage/PassMan-1.0.0.dmg
```

**Linux (DEB/RPM):**
```bash
./gradlew :desktop:jpackage --type deb
./gradlew :desktop:jpackage --type rpm
```

### Android Deployment

1. **Generate Signing Key:**
```bash
keytool -genkey -v -keystore passman-release. keystore \
  -alias passman -keyalg RSA -keysize 2048 -validity 10000
```

2. **Configure `android/app/build.gradle. kts`:**
```kotlin
signingConfigs {
    create("release") {
        storeFile = file("../../passman-release.keystore")
        storePassword = System.getenv("KEYSTORE_PASSWORD")
        keyAlias = "passman"
        keyPassword = System.getenv("KEY_PASSWORD")
    }
}
```

3. **Build Release APK:**
```bash
export KEYSTORE_PASSWORD=your_password
export KEY_PASSWORD=your_key_password
./gradlew :android:app:assembleRelease
```

4. **Upload to Google Play Console**

---

## 🗺️ Roadmap

### Version 1.0 (Current) ✅
- ✅ Core password management
- ✅ AES-256 encryption
- ✅ Local SQLite database
- ✅ Password generator
- ✅ Desktop JavaFX UI

### Version 1.5
- [ ] Secure vault for all kinds of files
- [ ] Android application MVP
- [ ] Cloud sync (Google Drive)
- [ ] Password reuse detection
- [ ] Basic analytics dashboard

### Version 2.0
- [ ] Password age tracking
- [ ] Dependency graph visualization
- [ ] QR code sharing
- [ ] Multi-user support

### Version 2.5
- [ ] Gamification modules
- [ ] Leaderboards (Firebase)
- [ ] Admin dashboard
- [ ] Security missions & quizzes
- [ ] Push notifications

### Version 3.0
- [ ] Browser extensions (Chrome/Firefox)
- [ ] Biometric authentication
- [ ] Hardware token support (YubiKey)
- [ ] Team/organization features
- [ ] HaveIBeenPwned integration
- [ ] Dark web monitoring

### Future Considerations
- [ ] iOS application
- [ ] Web vault (read-only)
- [ ] End-to-end encrypted sharing
- [ ] Password-less authentication (WebAuthn)
- [ ] Blockchain-based audit trail

---

## 🙏 Acknowledgments

- **BouncyCastle** - Cryptography library
- **ZXing** - QR code generation
- **Google Drive API** - Cloud backup integration
- **Firebase** - Leaderboard infrastructure
- **JavaFX Community** - UI framework support
- **SQLite** - Embedded database engine
- **JUnit & Mockito** - Testing frameworks

---

## 📞 Support & Contact

- **Author**: Abir Hasan Arko
- **GitHub**: [@AbirHasanArko](https://github.com/AbirHasanArko)
- **LinkedIn**: [@AbirHasanArko](https://www.linkedin.com/in/abirhasanarko/)
- **Location**: Khulna, Bangladesh
- **University**: CSE Undergrad @ KUET

### Getting Help

- 🐛 **Bug Reports**: [Open an issue](https://github.com/AbirHasanArko/PassMan/issues/new?template=bug_report.md)
- 💡 **Feature Requests**: [Request a feature](https://github.com/AbirHasanArko/PassMan/issues/new?template=feature_request.md)
- 💬 **Discussions**: [GitHub Discussions](https://github.com/AbirHasanArko/PassMan/discussions)

---

## 🌟 Star History

If you find PassMan useful, please consider giving it a ⭐ on GitHub! 

[![Star History Chart](https://api.star-history.com/svg?repos=AbirHasanArko/PassMan&type=Date)](https://star-history.com/#AbirHasanArko/PassMan&Date)

---

<div align="center">

**Built with ❤️ by [Abir Hasan Arko](https://github.com/AbirHasanArko)**

[⬆ Back to Top](#-passman-all-in-one-security-solution)

</div>
