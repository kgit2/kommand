pluginManagement {
    plugins {
        kotlin("jvm") version "1.9.21" apply false
        kotlin("multiplatform") version "1.9.21" apply false
        id("org.jetbrains.dokka") version "1.9.10" apply false
        id("io.github.gradle-nexus.publish-plugin") version "1.3.0" apply false
    }

    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

buildCache {
    local {
        directory = File(rootDir, "build-cache")
        removeUnusedEntriesAfterDays = 30
    }
}

rootProject.name = "kommand"

include(":kommand-examples")
include(":kommand-examples:example1")
include(":kommand-examples:example2")
include(":kommand-examples:example3")
