rootProject.name = "PassMan"

// --------------------------------------------------
// Plugin management (must come first)
// --------------------------------------------------
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

// --------------------------------------------------
// Dependency resolution management
// --------------------------------------------------
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        mavenCentral()
    }
}

// --------------------------------------------------
// Include subprojects
// --------------------------------------------------
include("core")
include("desktop")

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
