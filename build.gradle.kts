plugins {
    kotlin("multiplatform") version "1.7.20"
    application
}

group = "com.git-floater"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
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

        application {
            mainClass.set("MainKt")
        }
    }
    val hostOs = System.getProperty("os.name")
    val isMingw = hostOs.startsWith("Windows")
    val nativeTarget = when {
        hostOs == "Mac OS X" -> {
            if (System.getProperty("os.arch").contains("aarch64")) {
                macosArm64("native")
            } else {
                macosX64("native")
            }
        }
        hostOs == "Linux" -> linuxX64("native")
        isMingw -> {
            if (System.getenv("ProgramFiles(x86)") != null) {
                mingwX86("native")
            } else {
                mingwX64("native")
            }
        }
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    nativeTarget.apply {
        binaries {
            executable {
                entryPoint = "main"
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-core:2.1.2")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting
        val jvmTest by getting
        val nativeMain by getting
        val nativeTest by getting
    }
}
