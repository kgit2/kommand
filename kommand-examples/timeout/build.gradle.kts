import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val currentPlatform: Platform = when {
    DefaultNativePlatform.getCurrentOperatingSystem().isMacOsX && DefaultNativePlatform.getCurrentArchitecture().isAmd64 -> Platform.MACOS_X64
    DefaultNativePlatform.getCurrentOperatingSystem().isMacOsX && DefaultNativePlatform.getCurrentArchitecture().isArm64 -> Platform.MACOS_ARM64
    DefaultNativePlatform.getCurrentOperatingSystem().isLinux && DefaultNativePlatform.getCurrentArchitecture().isAmd64 -> Platform.LINUX_X64
    DefaultNativePlatform.getCurrentOperatingSystem().isLinux && DefaultNativePlatform.getCurrentArchitecture().isArm64 -> Platform.LINUX_ARM64
    DefaultNativePlatform.getCurrentOperatingSystem().isWindows && DefaultNativePlatform.getCurrentArchitecture().isAmd64 -> Platform.MINGW_X64
    else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
}

plugins {
    kotlin("multiplatform")
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

kotlin {
    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
        withJava()
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    val nativeTarget = when (currentPlatform) {
        Platform.MACOS_X64 -> macosX64()
        Platform.MACOS_ARM64 -> macosArm64()
        Platform.LINUX_X64 -> linuxX64()
        Platform.LINUX_ARM64 -> linuxArm64()
        Platform.MINGW_X64 -> mingwX64()
    }

    nativeTarget.apply {
        binaries {
            executable {
                entryPoint = "com.kgit2.kommand.main"
            }
        }
    }

    applyDefaultHierarchyTemplate()

    sourceSets {
        commonMain {
            dependencies {
                implementation(rootProject)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0-RC2")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")
            }
        }
    }
}

enum class Platform(
    val archName: String
) {
    MACOS_X64("x86_64-apple-darwin"),
    MACOS_ARM64("aarch64-apple-darwin"),
    LINUX_X64("x86_64-unknown-linux-gnu"),
    LINUX_ARM64("aarch64-unknown-linux-gnu"),
    MINGW_X64("x86_64-pc-windows-gnu"),
    ;
}
