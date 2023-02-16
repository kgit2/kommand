![logo](https://raw.githubusercontent.com/floater-git/Artist/main/kommand/logo.png)

# Kommand

Kotlin Native library for create subprocesses and handle their I/O.

# Supported Platforms

- macOS-x64
- macOS-arm64
- Linux-x64
- Mingw-x64
- JVM

# Architecture

![architecture](https://raw.githubusercontent.com/floater-git/Artist/main/kommand/architecture_2.0.png)

# Dependent

- Heavily inspired by the rust-std `Command`.
- Based on the `ktor-io`, Inter-Process Communication(IPC) can be handled using pipes
- Kotlin Multiplatform 1.7.20 with new memory manager

- ### Native for macOS/Linux

    System calls using POSIX api

- ### Native for Mingw

    System calls using Win32 api

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

[@XJMiada](https://github.com/XJMiada).(Original Picture)

## License

[Apache2.0](LICENSE) © BppleMan

## Credits

- [![JetBrains Logo (Main) logo](https://resources.jetbrains.com/storage/products/company/brand/logos/jb_beam.svg)](https://jb.gg/OpenSourceSupport)
