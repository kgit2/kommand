![logo](https://raw.githubusercontent.com/floater-git/Artist/main/Kommand/logo.png)

# Kommand
一个可以将外部命令跑在子进程的库，用于Kotlin Native/JVM

# 架构示意

![architecture](https://raw.githubusercontent.com/floater-git/Artist/main/Kommand/architecture.png)

# 源泉
- 深受rust-std `Command`启发。
- 基于ktor-io，可以使用管道处理进程间通信(IPC)。
- kotlin多平台1.7.20，使用新的内存管理器。

- ### Native for macOS/Linux/Mingw

    使用POSIX api的系统调用

- ### JVM

    基于 `java.lang.ProcessBuilder`

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
    implementation("com.git-floater:Kommand:$lastVersion")
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
