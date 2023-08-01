import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.dokka")
    `maven-publish`
    application
    signing
}

group = "com.kgit2"
version = "1.0.2"

repositories {
    mavenCentral()
    gradlePluginPortal()
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        withJava()
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    val nativeTargets = listOf(
        macosArm64(),
        macosX64(),
        linuxX64(),
        mingwX64(),
    )

    sourceSets {
        // add opt-in
        all {
            languageSettings.optIn("kotlinx.cinterop.UnsafeNumber")
            // languageSettings.optIn("kotlin.ExperimentalStdlibApi")
        }

        val commonMain by getting {
            dependencies {
                implementation("io.ktor:ktor-io:2.1.2")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val jvmMain by getting
        val jvmTest by getting

        val posixMain by creating {
            dependsOn(commonMain)
        }
        val posixTest by creating {
            dependsOn(commonTest)
            dependencies {
                implementation("io.ktor:ktor-server-core:2.1.2")
                implementation("io.ktor:ktor-server-cio:2.1.2")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
            }
        }

        val unixLikeMain by creating {
            dependsOn(posixMain)
        }
        val unixLikeTest by creating {
            dependsOn(posixTest)
        }
        val macosArm64Main by getting {
            dependsOn(unixLikeMain)
        }
        val macosArm64Test by getting {
            dependsOn(unixLikeTest)
        }
        val macosX64Main by getting {
            dependsOn(unixLikeMain)
        }
        val macosX64Test by getting {
            dependsOn(unixLikeTest)
        }
        val linuxX64Main by getting {
            dependsOn(unixLikeMain)
        }
        val linuxX64Test by getting {
            dependsOn(unixLikeTest)
        }
        val mingwX64Main by getting {
            dependsOn(posixMain)
        }
        val mingwX64Test by getting
    }
}

val subCommandInstallDist = tasks.findByPath(":sub_command:installDist")
tasks.forEach {
    if (it.group == "verification") {
        it.dependsOn(subCommandInstallDist)
    }
}

val ossrhUrl: String = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
val releasesRepoUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
val snapshotsRepoUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
val ossrhUsername: String by project
val ossrhPassword: String by project

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
