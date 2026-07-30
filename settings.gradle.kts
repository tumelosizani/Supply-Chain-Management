plugins {
    // Auto-provisions the Java 25 toolchain via the Foojay Disco API when it isn't installed locally.
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "scms"
