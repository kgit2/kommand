pluginManagement {
    plugins {
        kotlin("multiplatform") version "1.7.20" apply false
        id("org.jetbrains.dokka") version "1.7.20" apply false
    }

    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "kommand"

