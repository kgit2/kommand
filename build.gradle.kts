import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform
import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.dokka")
    id("io.github.gradle-nexus.publish-plugin")
    `maven-publish`
    signing
}

group = "com.kgit2"
version = "2.0.0"

repositories {
    mavenCentral()
    gradlePluginPortal()
}

subprojects {
    group = "com.kgit2"
    version = "1.2.0"
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "17"
        }
        withJava()
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    val nativeTargets = listOf(
        macosX64() to Platform.MACOS_X64,
        macosArm64() to Platform.MACOS_ARM64,
        linuxX64() to Platform.LINUX_X64,
        linuxArm64() to Platform.LINUX_ARM64,
        mingwX64() to Platform.MINGW_X64,
    )

    nativeTargets.forEach { (nativeTarget, targetPlatform) ->
        nativeTarget.apply {
            compilations.getByName("main") {
                cinterops {
                    create("kommandCore") {
                        defFile(project.file("src/nativeInterop/cinterop/${targetPlatform.archName}.def"))
                        packageName("kommand_core")
                    }
                }
            }
        }
    }

    applyDefaultHierarchyTemplate()

    sourceSets {
        // add opt-in
        all {
            languageSettings.optIn("kotlinx.cinterop.UnsafeNumber")
            languageSettings.optIn("kotlinx.cinterop.ExperimentalForeignApi")
            languageSettings.optIn("kotlin.experimental.ExperimentalNativeApi")
            languageSettings.optIn("kotlin.native.runtime.NativeRuntimeApi")
            languageSettings.optIn("kotlin.ExperimentalStdlibApi")

            languageSettings {
                compilerOptions {
                    freeCompilerArgs.add("-Xexpect-actual-classes")
                }
            }
        }

        commonMain {
            dependencies {
                implementation("org.jetbrains.kotlinx:atomicfu:0.23.1")
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

tasks {
    withType(Wrapper::class) {
        distributionType = Wrapper.DistributionType.ALL
        gradleVersion = "8.2"
    }

    create("buildKommandCore") {
        group = "kommand_core"
        doLast {
            buildKommandCore()
        }
    }

    forEach {
        if (it.group == "verification" || it.path.contains("Test")) {
            // it.dependsOn(buildKommandEcho)
        }
    }

    withType(Test::class) {
        testLogging {
            showStandardStreams = true
        }
    }

    // withType(KotlinNativeCompile::class) {
    //     compilerOptions {
    //         freeCompilerArgs.add("-Xexpect-actual-classes")
    //     }
    // }

    // withType(KotlinNativeLink::class) {
    //     doFirst {
    //         println(this.name)
    //         val targetPlatform = when (this.name) {
    //             "linkDebugTestMacosX64" -> Platform.MACOS_X64
    //             "linkDebugTestMacosArm64" -> Platform.MACOS_ARM64
    //             "linkDebugTestLinuxX64" -> Platform.LINUX_X64
    //             "linkDebugTestLinuxArm64" -> Platform.LINUX_ARM64
    //             "linkDebugTestMingwX64" -> Platform.MINGW_X64
    //             else -> throw GradleException("Unknown platform")
    //         }
    //         buildKommandCore(this.outputs.files.asPath, targetPlatform)
    //     }
    // }
}

val ossrhUrl: String = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
val releasesRepoUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
val snapshotsRepoUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
val ossrhUsername = runCatching {
    val ossrhUsername: String by project
    ossrhUsername
}.getOrNull()

val ossrhPassword = runCatching {
    val ossrhPassword: String by project
    ossrhPassword
}.getOrNull()

if (ossrhUsername != null && ossrhPassword != null) {
    val dokkaOutputDir = layout.buildDirectory.dir("dokka")

    tasks.getByName<DokkaTask>("dokkaHtml") {
        outputDirectory.set(file(dokkaOutputDir))
    }

    val deleteDokkaOutputDir by tasks.register<Delete>("deleteDokkaOutputDirectory") {
        delete(dokkaOutputDir)
    }

    val javadocJar = tasks.register<Jar>("javadocJar") {
        dependsOn(deleteDokkaOutputDir, tasks.dokkaHtml)
        archiveClassifier.set("javadoc")
        from(dokkaOutputDir)
    }

    nexusPublishing {
        repositories {
            sonatype {
                nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
                snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
                username.set(ossrhUsername)
                password.set(ossrhPassword)
            }
        }
    }

    publishing {
        repositories {
            mavenLocal()
            maven {
                url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
                credentials {
                    username = ossrhUsername
                    password = ossrhPassword
                }
            }
        }
        publications {
            withType<MavenPublication> {
                artifact(javadocJar.get())
                pom {
                    name.set("kommand")
                    description.set("A simple process library for Kotlin Multiplatform")
                    url.set("https://github.com/kgit2/kommand")
                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                    scm {
                        connection.set("https://github.com/kgit2/kommand.git")
                        url.set("https://github.com/kgit2/kommand")
                    }
                    developers {
                        developer {
                            id.set("BppleMan")
                            name.set("BppleMan")
                            email.set("bppleman@gmail.com")
                        }
                    }
                }
            }
        }
    }

    signing {
        // will default find the
        // - signing.keyId
        // - signing.password
        // - signing.secretKeyRingFile
        sign(publishing.publications)
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

val currentPlatform: Platform = when {
    DefaultNativePlatform.getCurrentOperatingSystem().isMacOsX && DefaultNativePlatform.getCurrentArchitecture().isAmd64 -> Platform.MACOS_X64
    DefaultNativePlatform.getCurrentOperatingSystem().isMacOsX && DefaultNativePlatform.getCurrentArchitecture().isArm64 -> Platform.MACOS_ARM64
    DefaultNativePlatform.getCurrentOperatingSystem().isLinux && DefaultNativePlatform.getCurrentArchitecture().isAmd64 -> Platform.LINUX_X64
    DefaultNativePlatform.getCurrentOperatingSystem().isLinux && DefaultNativePlatform.getCurrentArchitecture().isArm64 -> Platform.LINUX_ARM64
    DefaultNativePlatform.getCurrentOperatingSystem().isWindows && DefaultNativePlatform.getCurrentArchitecture().isAmd64 -> Platform.MINGW_X64
    else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
}

val platforms: List<Platform> = listOf(
    Platform.MACOS_X64,
    Platform.MACOS_ARM64,
    Platform.LINUX_X64,
    Platform.LINUX_ARM64,
    Platform.MINGW_X64,
)

fun buildKommandCore(targetPath: String? = null, targetPlatform: Platform? = null) {
    ProcessBuilder("just", "all")
        .directory(file("kommand-core"))
        .inheritIO()
        .start()
        .waitFor()
    if (targetPath != null && targetPlatform != null) {
        var kommandEchoName = "kommand-echo"
        if (targetPlatform == Platform.MINGW_X64) {
            kommandEchoName += ".exe"
        }
        file("kommand-core/target/${targetPlatform.archName}/release/${kommandEchoName}")
            .copyTo(file(targetPath).resolve(kommandEchoName), true)
    }
}
