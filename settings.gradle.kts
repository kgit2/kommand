pluginManagement {
    plugins {
        kotlin("jvm") version "1.9.0" apply false
        kotlin("multiplatform") version "1.9.0" apply false
        id("org.jetbrains.dokka") version "1.9.0" apply false
    }

    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "kommand"

include(":sub_command")
