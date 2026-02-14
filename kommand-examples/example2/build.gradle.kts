import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.KonanTarget

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

    // Ensure kommand-core static lib is built and linked for mingw target
    targets.withType(KotlinNativeTarget::class.java).configureEach {
        binaries.configureEach {
            if (konanTarget == KonanTarget.MINGW_X64) {
                // static-link libgcc/libstdc++
                linkerOpts.addAll(listOf("-static-libgcc", "-static-libstdc++"))

                // point to kommand-core static lib output (built by cargo)
                val coreLibDir = file("${rootProject.projectDir}/kommand-core/target/x86_64-pc-windows-gnu/release")
                linkerOpts.addAll(listOf("-L", coreLibDir.absolutePath, "-l:libkommand_core.a"))

                // include dir for generated header
                compilerOpts.addAll(listOf("-I", "${rootProject.projectDir}/kommand-core"))
            }
        }
    }

    applyDefaultHierarchyTemplate()

    sourceSets {
        commonMain {
            dependencies {
                implementation(rootProject)
            }
        }
    }
}

// Ensure kommand-core is built before assembling examples (only on Windows)
if (currentPlatform == Platform.MINGW_X64) {
    tasks.register("buildKommandCoreForWin") {
        doLast {
            exec {
                workingDir = file("${rootProject.projectDir}/kommand-core")
                environment("RUSTFLAGS", "-C link-arg=-static-libgcc -C link-arg=-static-libstdc++")
                commandLine("cargo", "build", "--release", "--target", "x86_64-pc-windows-gnu")
            }
        }
    }
    tasks.matching { it.name == "assemble" }.configureEach {
        dependsOn(tasks.named("buildKommandCoreForWin"))
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
