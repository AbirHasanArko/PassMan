plugins {
    java
}

group = "com.passman.core"
version = "1.0.0"

dependencies {
    implementation("org.xerial:sqlite-jdbc:3.43.0.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.bouncycastle:bcprov-jdk15on:1.70")
    testImplementation("org.mockito:mockito-core:5.5.0")
}