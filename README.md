[![Kommand Test](https://github.com/kgit2/kommand/actions/workflows/gradle.yml/badge.svg)](https://github.com/kgit2/kommand/actions/workflows/gradle.yml)

![logo](https://raw.githubusercontent.com/floater-git/Artist/main/kommand/logo.png)

# Kommand

Kotlin Native library for create subprocesses and handle their I/O.

# Supported Platforms

- macOS-X64
- macOS-Arm64
- linux-X64
- linux-Arm64
- mingw-X64
- JVM

# Architecture

![architecture](assets/architecture_3.0.png)

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
child.wait()
```

### Null I/O

```kotlin
Command("gradle")
    .arg("build")
    .stdout(Stdio.Null)
    .spawn()
    .wait()
```

## Build

### 1. clone this repo

```bash
git clone https://github.com/kgit2/kommand.gi
```
Then you can build it with gradle

```bash
./gradlew build
```

> If you want to unit test it, go on.

### 2. install dependencies

* install rust toolchain

```bash
curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh
```

* install cross & justfile

```bash
cargo install cross justfile
```

* install docker

[Install Docker Engine](https://docs.docker.com/engine/install/)

* build `eko`

```bash
cd eko
just prepare # install some toolchains
just all # for all platforms
# also can build for specific platform
just macosX64
just linuxX64
#or else
just --list
```
* test it

```bash
just linuxX64Test
# or
just macosX64Test
# or else
just --list
```

## Maintainers

[@BppleMan](https://github.com/BppleMan).

[@XJMiada](https://github.com/XJMiada).(Original Picture)

## License

[Apache2.0](LICENSE) © BppleMan

## Credits

- [![JetBrains Logo (Main) logo](https://resources.jetbrains.com/storage/products/company/brand/logos/jb_beam.svg)](https://jb.gg/OpenSourceSupport)

## Star History

[![Star History Chart](https://api.star-history.com/svg?repos=kgit2/kommand&type=Date&theme=dark)](https://star-history.com/#kgit2/kommand&Date)
