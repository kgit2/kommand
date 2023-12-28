[![Kommand Publish](https://github.com/kgit2/kommand/actions/workflows/gradle-publish.yml/badge.svg?branch=main)](https://github.com/kgit2/kommand/actions/workflows/gradle-publish.yml)
[![Kommand Test](https://github.com/kgit2/kommand/actions/workflows/gradle.yml/badge.svg?branch=test)](https://github.com/kgit2/kommand/actions/workflows/gradle.yml)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.kgit2/kommand/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.kgit2/kommand)

![logo](https://raw.githubusercontent.com/floater-git/Artist/main/kommand/logo.png)

# Kommand

一个可以将外部命令跑在子进程的库，用于Kotlin Native/JVM

# v2.0.0

Rust 是一门兼顾性能和工程性的优秀语言。

在 1.x 版本中，我们使用以下 API 来提供创建子进程的功能

- `fork` of [POSIX api]
- `CreateChildProcess` of [win32 api]
- `java.lang.ProcessBuilder` of JVM

在 2.0 版本中，我们使用 Rust 标准库来提供创建子进程的功能。

- `std::process::Command` of Rust
- `java.lang.ProcessBuilder` of JVM

它将带来

- 更统一的 API
- 更易用的 API
- 性能依旧优秀
- 更易于维护
- 代码结构更清晰

# 支持的平台

- x86_64-apple-darwin
- aarch64-apple-darwin
- x86_64-unknown-linux-gnu
- aarch64-unknown-linux-gnu
- x86_64-pc-windows-gnu (mingw-w64)
- jvm

# 依赖于

- Rust Standard Library 1.69.0
- Kotlin Multiplatform 1.9.21

# 用法

## 依赖

`build.gradle.kts`:

```kotlin
// ……
repositories {
    mavenCentral()
}
// ……

dependencies {
    // should replace with the latest version
    implementation("com.kgit2:kommand:2.x")
}

```

## 快速上手

### 继承标准 I/O

https://github.com/kgit2/kommand/blob/af7c721f774550d0d5f72758f101074c12fae134/kommand-examples/example1/src/commonMain/kotlin/com/kgit2/kommand/Main.kt#L1-L12


### 管道 I/O

https://github.com/kgit2/kommand/blob/af7c721f774550d0d5f72758f101074c12fae134/kommand-examples/example2/src/commonMain/kotlin/com/kgit2/kommand/Main.kt#L1-L15


### 屏蔽 I/O

https://github.com/kgit2/kommand/blob/af7c721f774550d0d5f72758f101074c12fae134/kommand-examples/example3/src/commonMain/kotlin/com/kgit2/kommand/Main.kt#L1-L12

### 超时检测

https://github.com/kgit2/kommand/blob/7367c60db7b3475be0de17474dbcec3d518894ba/kommand-examples/timeout/src/appleMain/kotlin/com/kgit2/kommand/Main.kt#L1-L60

完成示例 [kommand-examples/timeout](kommand-examples/timeout).

依赖:

- `required` [kotlinx-coroutines](https://github.com/Kotlin/kotlinx.coroutines)

## 自行编译

### 1. 依赖

- rust toolchain - <= 1.69.0 (https://rustup.rs) (建议)
    - cross (install with `cargo install cross`)
    - just (install with `cargo install just`)
- 交叉编译工具链
    - x86_64-apple-darwin
    - aarch64-apple-darwin
    - x86_64-unknown-linux-gnu
    - aarch64-unknown-linux-gnu
    - x86_64-pc-windows-gnu (mingw-w64)
- docker (可选)

强烈推荐在 macOS 编译所有平台。

Kotlin Multiplatform 在 macOS 有更好的支持

> 如果你使用 macOS , 你可以用下述命令安装工具链
> ```bash
> just prepare
> ```
> 否则, 你需要自行安装工具链

### 2. 克隆仓库

```bash
git clone https://github.com/kgit2/kommand.git
```
### 3. 编译 kommand-core

```bash
cd kommand-core
just all
```

### 4. 编译 kommand

```bash
./gradlew build
```

### 5. 跨平台测试

> 仅 linux 平台支持跨平台测试.

* install docker

[Install Docker Engine](https://docs.docker.com/engine/install/)

* 运行测试

```bash
# for x86_64
just linuxX64Test
# for aarch64
just linuxArm64Test
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
