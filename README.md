![logo](https://raw.githubusercontent.com/floater-git/Artist/main/kommand/logo.png)

# Kommand

Kotlin Native library for run external command

# Architecture

![architecture](https://raw.githubusercontent.com/floater-git/Artist/main/kommand/architecture.png)

# Dependent

- Heavily inspired by the rust-std `Command`.
- Based on the `ktor-io`, Inter-Process Communication(IPC) can be handled using pipes
- Kotlin Multiplatform 1.7.20 with new memory manager

- ### Native for macOS/Linux/Mingw

    System calls using POSIX api

- ### JVM

    Based `java.lang.ProcessBuilder`


# Usage

## Dependency

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.kgit2/kommand/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.kgit2/kommand)

`build.gradle.kts`:

```kotlin
// ……
repositories {
    mavenCentral()
}
// ……

dependencies {
    implementation("com.kgit2:kommand:$lastVersion")
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
val stdoutReader: com.kgit2.io.Reader? = child.getChildStdout()
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

## Maintainers

[@BppleMan](https://github.com/BppleMan).

## License

[MIT](LICENSE) © BppleMan
