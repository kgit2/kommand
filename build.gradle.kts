import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform
import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.dokka")
    id("io.github.gradle-nexus.publish-plugin")
    `maven-publish`
    application
    signing
}

group = "com.kgit2"
version = "1.2.0"

val mainHost = Platform.MACOS_X64
val targetPlatform = Platform.valueOf(project.findProperty("targetPlatform")?.toString() ?: "MACOS_X64")
// for debug
// val targetPlatform = Platform.valueOf(project.findProperty("targetPlatform")?.toString() ?: "MACOS_ARM64")
// val targetPlatform = Platform.valueOf(project.findProperty("targetPlatform")?.toString() ?: "LINUX_X64")
// val targetPlatform = Platform.valueOf(project.findProperty("targetPlatform")?.toString() ?: "LINUX_ARM64")
// val targetPlatform = Platform.valueOf(project.findProperty("targetPlatform")?.toString() ?: "MINGW_X64")

val ktorIO = "2.3.4"

repositories {
    mavenCentral()
    gradlePluginPortal()
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

    val nativeTarget = when (targetPlatform) {
        Platform.MACOS_X64 -> macosX64("native")
        Platform.MACOS_ARM64 -> macosArm64("native")
        Platform.LINUX_X64 -> linuxX64("native")
        Platform.LINUX_ARM64 -> linuxArm64("native")
        Platform.MINGW_X64 -> mingwX64("native")
    }

    nativeTarget.apply {
        compilations.getByName("main") {
            cinterops {
                val kommandCore by creating {
                    if (targetPlatform.toString().contains("macos")) {
                        defFile(project.file("src/nativeInterop/cinterop/macos.def"))
                    } else {
                        defFile(project.file("src/nativeInterop/cinterop/${targetPlatform}.def"))
                    }
                    packageName("kommand_core")
                }
            }
        }
    }

    sourceSets {
        // add opt-in
        all {
            languageSettings.optIn("kotlinx.cinterop.UnsafeNumber")
            languageSettings.optIn("kotlinx.cinterop.ExperimentalForeignApi")
            languageSettings.optIn("kotlin.experimental.ExperimentalNativeApi")
            languageSettings.optIn("kotlin.native.runtime.NativeRuntimeApi")
            languageSettings.optIn("kotlin.ExperimentalStdlibApi")
        }

        val commonMain by getting {
            dependencies {
                implementation("io.ktor:ktor-io:2.3.4")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val jvmMain by getting
        val jvmTest by getting

        val targetSourceSetName = when (targetPlatform) {
            Platform.MACOS_X64 -> "macosX64"
            Platform.MACOS_ARM64 -> "macosArm64"
            Platform.LINUX_X64 -> "linuxX64"
            Platform.LINUX_ARM64 -> "linuxArm64"
            Platform.MINGW_X64 -> "mingwX64"
        }

        val targetMain = create("${targetSourceSetName}Main") {
            dependsOn(commonMain)
        }
        val targetTest = create("${targetSourceSetName}Test") {
            dependsOn(commonTest)
        }

        val nativeMain by getting {
            dependsOn(targetMain)
        }
        val nativeTest by getting {
            dependsOn(targetTest)
        }
    }
}

val subCommandInstallDist = tasks.findByPath(":sub_command:installDist")

val buildEko = tasks.create("buildEko") {
    group = "build"
    doLast {
        ProcessBuilder("bash", "-c", "cargo build --release")
            .directory(file("eko"))
            .inheritIO()
            .start()
            .waitFor()
    }
}

tasks.forEach {
    if (it.group == "verification" || it.path.contains("Test")) {
        it.dependsOn(buildEko)
    }
}

tasks {
    val wrapper by getting(Wrapper::class) {
        distributionType = Wrapper.DistributionType.ALL
        gradleVersion = "8.5"
    }
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
    val keyId = project.findProperty("signing.keyId") as String?
    val keyPass = project.findProperty("signing.password") as String?
    val keyRingFile = project.findProperty("signing.secretKeyRingFile") as String?

    val dokkaOutputDir = "$buildDir/dokka"

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
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
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

    override fun toString(): String {
        return when (this) {
            MACOS_X64 -> "macosx64"
            MACOS_ARM64 -> "macosarm64"
            LINUX_X64 -> "linuxx64"
            LINUX_ARM64 -> "linuxarm64"
            MINGW_X64 -> "mingw64"
        }
    }
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
