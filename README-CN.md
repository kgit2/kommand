![logo](https://raw.githubusercontent.com/floater-git/Artist/main/kommand/logo.png)

# Kommand
一个可以将外部命令跑在子进程的库，用于Kotlin Native/JVM

# 架构示意

![architecture](https://raw.githubusercontent.com/floater-git/Artist/main/kommand/architecture_2.0.png)

# 源泉
- 深受rust-std `Command`启发。
- 基于ktor-io，可以使用管道处理进程间通信(IPC)。
- kotlin多平台1.7.20，使用新的内存管理器。

- ### Native for macOS/Linux

    使用POSIX api的系统调用

- ### Native for Mingw

    使用Win32 api的系统调用

- ### JVM

    基于 `java.lang.ProcessBuilder`

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

## 主要贡献者

[@BppleMan](https://github.com/BppleMan).

[@XJMiada](https://github.com/XJMiada).(图片原创)

## 许可证

[Apache2.0](LICENSE) © BppleMan

## 感谢

- [![JetBrains Logo (Main) logo](https://resources.jetbrains.com/storage/products/company/brand/logos/jb_beam.svg)](https://jb.gg/OpenSourceSupport)
