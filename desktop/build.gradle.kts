import java.time.LocalDateTime

plugins {
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
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

    // For Charts (Analytics)
    // JavaFX already includes charts, no extra dependency

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
        "javafx.web"  // Added - contains javafx.scene.text.Font and other text components
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
    description = "Create a fat JAR with all dependencies (JavaFX may not run standalone)"

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
        """.trimMargin())
    }
}

tasks.processResources {
    include("**/*.fxml", "**/*.css", "**/*.png", "**/*.jpg", "**/*.properties")
}

tasks.register("dev") {
    group = "application"
    description = "Run application in development mode"
    dependsOn("classes")
    finalizedBy("run")
}
