import java.time.LocalDateTime

plugins {
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("org.beryx.runtime") version "1.13.1"  // For jpackage support
}

group = "com.passman"
version = "1.0.0"

dependencies {
    implementation(project(":core"))

    testImplementation(platform("org.junit:junit-bom:5.10.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.testfx:testfx-core:4.0.18")
    testImplementation("org.testfx:testfx-junit5:4.0.18")

    // For QR Code generation
    implementation("com.google.zxing:core:3.5.2")
    implementation("com.google.zxing:javase:3.5.2")

    // For better date/time handling
    implementation("org.threeten:threeten-extra:1.7.2")
}

javafx {
    version = "21"
    modules = listOf(
        "javafx.controls",
        "javafx.fxml",
        "javafx.graphics",
        "javafx.base",
        "javafx.web",
        "javafx.swing"
    )
}

application {
    mainClass.set("com.passman.desktop.MainApp")

    applicationDefaultJvmArgs = listOf(
        "-Xmx512m",
        "-Xms256m",
        "--add-exports", "javafx.base/com.sun.javafx.event=ALL-UNNAMED",
        "--add-opens", "javafx.graphics/javafx.scene.text=ALL-UNNAMED",
        "--add-opens", "javafx.graphics/com.sun.javafx.scene.text=ALL-UNNAMED"
    )
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-Xlint:deprecation")
    options.compilerArgs.add("-Xlint:unchecked")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    withSourcesJar()
}

// ============================================
// Runtime & Installer Configuration
// ============================================
runtime {
    options.set(listOf(
        "--strip-debug", 
        "--compress", "2", 
        "--no-header-files", 
        "--no-man-pages",
        // Bind services - critical for ImageIO to find PNG/JPEG readers/writers
        "--bind-services"
    ))
    
    // Include all modules needed for full functionality including ZXing QR codes
    // The --bind-services option will automatically include service providers
    modules.set(listOf(
        "java.base",
        "java.desktop",      // Required for AWT/ImageIO
        "java.logging",
        "java.sql",
        "java.naming",
        "java.xml",
        "java.scripting",
        "java.prefs",
        "java.datatransfer",
        "java.compiler",
        "java.management",
        "jdk.unsupported",
        "jdk.crypto.ec",
        "jdk.crypto.mscapi",  // Windows crypto support
        "jdk.localedata",
        "jdk.charsets"        // Character sets for encoding
    ))
    
    // Automatically detect and add required modules from dependencies
    additive.set(true)
    
    launcher {
        noConsole = false  // Show console for debugging (set to true for release)
    }

    jpackage {
        // Use Launcher class instead of MainApp (jpackage fix for JavaFX)
        mainClass = "com.passman.desktop.Launcher"
        
        // Basic app info
        jpackageHome = System.getenv("JAVA_HOME") ?: ""
        imageName = "PassMan"
        
        // Icon (create this file for custom icon)
        val iconFile = file("src/main/resources/icons/icon.ico")
        
        imageOptions = buildList {
            add("--app-version")
            add(project.version.toString())
            add("--vendor")
            add("Abir Hasan Arko")
            add("--copyright")
            add("Copyright 2026 PassMan")
            add("--description")
            add("PassMan - All-in-One Security Solution")
            if (iconFile.exists()) {
                add("--icon")
                add(iconFile.absolutePath)
            }
        }
        
        // Windows-specific installer options
        installerOptions = listOf(
            "--win-dir-chooser",
            "--win-menu",
            "--win-shortcut",
            "--win-shortcut-prompt"
        )
    }
}

// ============================================
// Additional Tasks
// ============================================
tasks.register<JavaExec>("runDebug") {
    group = "application"
    description = "Run the application with debug logging"

    mainClass.set("com.passman.desktop.MainApp")
    classpath = sourceSets["main"].runtimeClasspath

    jvmArgs = listOf(
        "-Xmx512m",
        "-Djavafx.verbose=true",
        "-Dprism.verbose=true",
        "--add-opens", "javafx.graphics/javafx.scene.text=ALL-UNNAMED"
    )
}

tasks.register<Jar>("fatJar") {
    group = "build"
    description = "Create a fat JAR with all dependencies"

    archiveBaseName.set("PassMan")
    archiveVersion.set(project.version.toString())
    archiveClassifier.set("standalone")

    from(
        configurations.runtimeClasspath.get().map {
            if (it.isDirectory) it else zipTree(it)
        }
    )

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    manifest {
        attributes(
            "Main-Class" to "com.passman.desktop.MainApp",
            "Implementation-Title" to "PassMan",
            "Implementation-Version" to project.version,
            "Built-By" to System.getProperty("user.name"),
            "Built-Date" to LocalDateTime.now().toString()
        )
    }

    with(tasks.jar.get())
}

tasks.register("appInfo") {
    group = "help"
    doLast {
        println("""
            |=====================================
            |  PassMan Desktop Application
            |=====================================
            |  Version: ${project.version}
            |  Java Version: ${java.sourceCompatibility}
            |  JavaFX Version: 21
            |  Main Class: com.passman.desktop.MainApp
            |  Build Date: ${LocalDateTime.now()}
            |=====================================
            |
            |  Available installer tasks:
            |  - runtime       : Create custom JRE
            |  - runtimeZip    : Create portable ZIP
            |  - jpackage      : Create native installer (needs WiX)
            |  - jpackageImage : Create app folder (no WiX needed)
            |=====================================
        """.trimMargin())
    }
}

tasks.processResources {
    include("**/*.fxml", "**/*.css", "**/*.png", "**/*.jpg", "**/*.ico", "**/*.properties")
}

tasks.register("dev") {
    group = "application"
    description = "Run application in development mode"
    dependsOn("classes")
    finalizedBy("run")
}

// ============================================
// Custom jpackage tasks (more reliable for JavaFX)
// ============================================
tasks.register<Exec>("createInstaller") {
    group = "distribution"
    description = "Create EXE installer using jpackage with Launcher class"
    dependsOn("jar")
    
    val jpackagePath = System.getenv("JAVA_HOME")?.let { "$it\\bin\\jpackage.exe" } ?: "jpackage"
    val libsDir = layout.buildDirectory.dir("libs").get().asFile
    val outputDir = layout.buildDirectory.dir("installer").get().asFile
    val iconFile = file("src/main/resources/icons/icon.ico")
    
    // Collect all runtime dependencies
    doFirst {
        outputDir.mkdirs()
        // Copy all dependencies to libs folder
        copy {
            from(configurations.runtimeClasspath)
            into(libsDir)
        }
    }
    
    commandLine = buildList {
        add(jpackagePath)
        add("--type"); add("exe")
        add("--name"); add("PassMan")
        add("--app-version"); add(project.version.toString())
        add("--vendor"); add("Abir Hasan Arko")
        add("--description"); add("PassMan - All-in-One Security Solution")
        add("--copyright"); add("Copyright 2026 PassMan")
        add("--input"); add(libsDir.absolutePath)
        add("--main-jar"); add("desktop-${project.version}.jar")
        add("--main-class"); add("com.passman.desktop.Launcher")
        add("--dest"); add(outputDir.absolutePath)
        add("--java-options"); add("-Xmx512m")
        add("--java-options"); add("-Xms256m")
        if (iconFile.exists()) {
            add("--icon"); add(iconFile.absolutePath)
        }
        add("--win-dir-chooser")
        add("--win-menu")
        add("--win-shortcut")
        add("--win-shortcut-prompt")
    }
}

tasks.register<Exec>("createPortable") {
    group = "distribution"
    description = "Create portable app folder using jpackage with Launcher class"
    dependsOn("jar")
    
    val jpackagePath = System.getenv("JAVA_HOME")?.let { "$it\\bin\\jpackage.exe" } ?: "jpackage"
    val libsDir = layout.buildDirectory.dir("libs").get().asFile
    val outputDir = layout.buildDirectory.dir("portable").get().asFile
    val iconFile = file("src/main/resources/icons/icon.ico")
    
    doFirst {
        outputDir.mkdirs()
        // Copy all dependencies to libs folder
        copy {
            from(configurations.runtimeClasspath)
            into(libsDir)
        }
    }
    
    commandLine = buildList {
        add(jpackagePath)
        add("--type"); add("app-image")
        add("--name"); add("PassMan")
        add("--app-version"); add(project.version.toString())
        add("--vendor"); add("Abir Hasan Arko")
        add("--description"); add("PassMan - All-in-One Security Solution")
        add("--input"); add(libsDir.absolutePath)
        add("--main-jar"); add("desktop-${project.version}.jar")
        add("--main-class"); add("com.passman.desktop.Launcher")
        add("--dest"); add(outputDir.absolutePath)
        add("--java-options"); add("-Xmx512m")
        add("--java-options"); add("-Xms256m")
        if (iconFile.exists()) {
            add("--icon"); add(iconFile.absolutePath)
        }
    }
}
