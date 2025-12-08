plugins {
    java
}

group = "com.passman"
version = "1.0.0"

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    dependencies {
        testImplementation("org.junit.jupiter:junit-jupiter:5.9.3")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    }

    tasks. test {
        useJUnitPlatform()
    }
}