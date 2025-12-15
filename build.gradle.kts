plugins {
    java
}

group = "com.passman"
version = "1.0.0"

subprojects {

    apply(plugin = "java-library")

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.compilerArgs.addAll(
            listOf(
                "-Xlint:deprecation",
                "-Xlint:unchecked",
                "-parameters"
            )
        )
    }

    tasks.withType<Test> {
        useJUnitPlatform()

        testLogging {
            events("passed", "skipped", "failed")
            exceptionFormat =
                org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
            showExceptions = true
            showCauses = true
            showStackTraces = true
        }

        maxParallelForks =
            (Runtime.getRuntime().availableProcessors() / 2).takeIf { it > 0 } ?: 1
    }
}

// Root project tasks
tasks.register("cleanAll") {
    group = "build"
    description = "Clean all subprojects"
    dependsOn(subprojects.map { it.tasks.named("clean") })
}

tasks.register("buildAll") {
    group = "build"
    description = "Build all subprojects"
    dependsOn(subprojects.map { it.tasks.named("build") })
}

tasks.register("testAll") {
    group = "verification"
    description = "Run all tests in all subprojects"
    dependsOn(subprojects.map { it.tasks.named("test") })
}

tasks.register("info") {
    group = "help"
    description = "Display project information"

    doLast {
        println(
            """
            |
            |╔════════════════════════════════════════════════════════════╗
            |║                                                            ║
            |║     🔐  PassMan - Secure Password Manager                 ║
            |║                                                            ║
            |╚════════════════════════════════════════════════════════════╝
            |
            |  Version:            ${project.version}
            |  Java Version:      ${JavaVersion.current()}
            |  Gradle Version:    ${gradle.gradleVersion}
            |  Operating System:  ${System.getProperty("os.name")} ${System.getProperty("os.version")}
            |  
            |  Modules:
            |    ├── core        (Backend - Encryption, Database, Services)
            |    └── desktop     (JavaFX UI Application)
            |
            |════════════════════════════════════════════════════════════════
            |
            """.trimMargin()
        )
    }
}

tasks.register("stats") {
    group = "help"
    description = "Display project statistics"

    doLast {
        var totalFiles = 0
        var totalLines = 0
        var javaFiles = 0
        var kotlinFiles = 0
        var xmlFiles = 0
        var cssFiles = 0

        subprojects.forEach { subproject ->
            subproject.fileTree("src/main").matching {
                include("**/*.java", "**/*.kt", "**/*.fxml", "**/*.css", "**/*.sql")
            }.forEach { file ->
                totalFiles++
                totalLines += file.readLines().size

                when (file.extension) {
                    "java" -> javaFiles++
                    "kt" -> kotlinFiles++
                    "fxml" -> xmlFiles++
                    "css" -> cssFiles++
                }
            }
        }

        println(
            """
            |
            |Project Statistics:
            |  Total Files:        $totalFiles
            |  Total Lines:       $totalLines
            |  
            |  By Type:
            |    Java Files:      $javaFiles
            |    Kotlin Files:    $kotlinFiles
            |    FXML Files:      $xmlFiles
            |    CSS Files:       $cssFiles
            |
            """.trimMargin()
        )
    }
}

// Wrapper configuration
tasks.wrapper {
    gradleVersion = "8.5"
    distributionType = Wrapper.DistributionType.ALL
}

// Default task
defaultTasks("info")
