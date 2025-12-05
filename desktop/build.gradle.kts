plugins {
    id("java")
}

group = "com.arko.passman"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":core"))
    // add JavaFX and other deps later, e.g.:
    // implementation("org.openjfx:javafx-controls:20")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

//application {
    // mainClass.set("com.passman.desktop.MainApp") // set after you create MainApp
//}

tasks.test {
    useJUnitPlatform()
}