rootProject.name = "PassMan"

// --------------------------------------------------
// Plugin management (must come first)
// --------------------------------------------------
pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
    plugins {
        id("com.android.application") version "8.13.2"
    }
}

// --------------------------------------------------
// Dependency resolution management
// --------------------------------------------------
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

// --------------------------------------------------
// Include subprojects
// --------------------------------------------------
include("core")
include("desktop")
include("android")

// --------------------------------------------------
// Optional Gradle features
// --------------------------------------------------
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

// --------------------------------------------------
// Build cache configuration
// --------------------------------------------------
buildCache {
    local {
        isEnabled = true
        directory = File(rootDir, ".gradle/build-cache")
        removeUnusedEntriesAfterDays = 30
    }
}

// --------------------------------------------------
// Display welcome message
// --------------------------------------------------
gradle.projectsLoaded {
    println(
        """
        ╔════════════════════════════════════════════╗
        ║   🔐  PassMan Build Configuration         ║
        ╚════════════════════════════════════════════╝
        
        Modules:  ${rootProject.subprojects.size}
          ├── core     (Backend)
          └── desktop  (UI)
        
        Build cache:  Enabled
        """.trimIndent()
    )
}
