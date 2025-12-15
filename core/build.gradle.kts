import java.time.LocalDateTime

plugins {
    java
}

group = "com.passman"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    // JSON processing (for identity cards and backup metadata)
    implementation("com.google.code.gson:gson:2.10.1")

    // SQLite JDBC driver
    implementation("org.xerial:sqlite-jdbc:3.45.0.0")

    // Testing
    testImplementation(platform("org.junit:junit-bom:5.10.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.mockito:mockito-core:5.8.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.8.0")
}

tasks.test {
    useJUnitPlatform()

    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
        showExceptions = true
        showCauses = true
        showStackTraces = true
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-Xlint:deprecation")
    options.compilerArgs.add("-Xlint:unchecked")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17

    // Enable reproducible builds
    withSourcesJar()
    withJavadocJar()
}

// Custom task to run specific test
tasks.register("testCrypto", Test::class) {
    useJUnitPlatform()
    filter {
        includeTestsMatching("com.passman.core.crypto.*")
    }
}

tasks.register("testDatabase", Test::class) {
    useJUnitPlatform()
    filter {
        includeTestsMatching("com.passman.core.db.*")
    }
}

// Code coverage (optional - for future integration)
// Uncomment when you want to add JaCoCo code coverage
/*
plugins {
    jacoco
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}
*/

// Generate build info
tasks.register("buildInfo") {
    doLast {
        println("=====================================")
        println("  PassMan Core Module")
        println("=====================================")
        println("  Version: ${project.version}")
        println("  Java Version: ${java.sourceCompatibility}")
        println("  Build Time: ${LocalDateTime.now()}")
        println("=====================================")
    }
}

tasks.build {
    dependsOn("buildInfo")
}