plugins {
    kotlin("jvm") version "2.1.20"
}

group = "eu.wynq"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("org.jetbrains.pty4j:pty4j:0.13.4")
    implementation("org.slf4j:slf4j-nop:2.0.13")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}
