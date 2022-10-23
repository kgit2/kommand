# Kommand

Kotlin Native library for run external command

# Dependent

- Heavily inspired by the rust-std `Command`.
- Based on the `ktor-io`, Inter-Process Communication(IPC) can be handled using pipes
- Kotlin Multiplatform 1.7.20 with new memory manager

## Native for macOS/Linux/Mingw

- System calls using POSIX api

## JVM

- Based `java.lang.ProcessBuilder`


# Usage

## Dependency

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.git-floater/Kommand/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.git-floater/Kommand)

`build.gradle.kts`:

```kotlin
// ……
repositories {
    mavenCentral()
}
// ……

dependencies {
    implementation("com.git-floater:Kommand:0.0.1")
}

```

## Quick Start

### Inherit Standard I/O

```kotlin
Command("ping")
    .arg("-c")
    .args("5", "localhost")
    .spawn()
    .wait()
```

### Piped I/O

```kotlin
val child = Command("ping")
    .args("-c", "5", "localhost")
    .stdout(Stdio.Pipe)
    .spawn()
val stdoutReader: com.floater.io.Reader? = child.getChildStdout()
val lines: Sequence<String> = stdoutReader?.lines()
lines.forEach { 
    println(it)
}
```

### Null I/O

```kotlin
Command("gradle")
    .arg("build")
    .stdout(Stdio.Null)
    .spawn()
    .wait()
```
