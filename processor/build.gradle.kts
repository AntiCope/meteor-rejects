plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp")
}

group = "com.github.shu"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.devtools.ksp:symbol-processing-api:2.0.21-1.0.25")
}