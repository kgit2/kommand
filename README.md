[![Kommand Publish](https://github.com/kgit2/kommand/actions/workflows/kommand%20publish.yml/badge.svg)](https://github.com/kgit2/kommand/actions/workflows/kommand%20publish.yml)
[![Kommand Test](https://github.com/kgit2/kommand/actions/workflows/kommand%20test.yml/badge.svg?branch=main)](https://github.com/kgit2/kommand/actions/workflows/kommand%20test.yml)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.kgit2/kommand/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.kgit2/kommand)


[![ko-fi](https://ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/B0B2SNCVJ)

![logo](https://raw.githubusercontent.com/floater-git/Artist/main/kommand/logo.png)

# Kommand

Kotlin Native library for create sub-process and redirect their I/O.

# v2.3.0

Rust is an excellent language that takes into account both performance and engineering.

In version 1.x, we use the following API to provide the function of creating child processes

- `fork` of [POSIX api]
- `CreateChildProcess` of [win32 api]
- `java.lang.ProcessBuilder` of JVM

In version 2.x, we use the Rust standard library to provide the function of creating child processes.

- `std::process::Command` of Rust
- `java.lang.ProcessBuilder` of JVM

It will bring

- More unified API
- Easier to use API
- Performance is still excellent
- Easier to maintain
- Code structure is clearer

# Supported Platforms

- x86_64-apple-darwin
- aarch64-apple-darwin
- x86_64-unknown-linux-gnu
- aarch64-unknown-linux-gnu
- x86_64-pc-windows-gnu (mingw-w64)
- jvm

> **Note for Windows users**: See [Windows Build Guide](docs/WINDOWS.md) for information about static linking and the `libstdc++-6.dll` issue.

# Dependent

- Rust Standard Library 1.83.0
- Kotlin Multiplatform 2.1.0

# Usage

## Dependency

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

## Quick Start

### Inherit Standard I/O

https://github.com/kgit2/kommand/blob/af7c721f774550d0d5f72758f101074c12fae134/kommand-examples/example1/src/commonMain/kotlin/com/kgit2/kommand/Main.kt#L1-L12


### Piped I/O

https://github.com/kgit2/kommand/blob/af7c721f774550d0d5f72758f101074c12fae134/kommand-examples/example2/src/commonMain/kotlin/com/kgit2/kommand/Main.kt#L1-L15


### Null I/O

https://github.com/kgit2/kommand/blob/af7c721f774550d0d5f72758f101074c12fae134/kommand-examples/example3/src/commonMain/kotlin/com/kgit2/kommand/Main.kt#L1-L12

### Timeout Detection

https://github.com/kgit2/kommand/blob/7367c60db7b3475be0de17474dbcec3d518894ba/kommand-examples/timeout/src/appleMain/kotlin/com/kgit2/kommand/Main.kt#L1-L60

Full example check [kommand-examples/timeout](kommand-examples/timeout).

Dependency:

- `required` [kotlinx-coroutines](https://github.com/Kotlin/kotlinx.coroutines)

## Build by yourself

### 1. Dependencies

- rust toolchain - 1.83.0 (https://rustup.rs)
  - just (install with `cargo install just`)
- cross-compile toolchain
  - x86_64-apple-darwin
  - aarch64-apple-darwin
  - x86_64-unknown-linux-gnu
  - aarch64-unknown-linux-gnu
  - x86_64-pc-windows-gnu (mingw-w64)
- docker (optional)

Recommend build all platforms in macOS. 

Kotlin Multiplatform gets the most complete support on macOS.

> If you are using macOS, you can install the cross-compile toolchain with
> ```bash
> just prepare
> ```
> Otherwise, you need to install the cross-compile toolchain yourself.

### 2. Clone this repo

```bash
git clone https://github.com/kgit2/kommand.git
```
### 3. Build kommand-core

```bash
cd kommand-core
just all
```

### 4. Build kommand

```bash
./gradlew build
```

### 5. cross-platform test

> Only linux support cross-platform test.

* install docker

[Install Docker Engine](https://docs.docker.com/engine/install/)

* test it

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

<a href="https://star-history.com/#kgit2/kommand&Date">
 <picture>
   <source media="(prefers-color-scheme: dark)" srcset="https://api.star-history.com/svg?repos=kgit2/kommand&type=Date&theme=dark" />
   <source media="(prefers-color-scheme: light)" srcset="https://api.star-history.com/svg?repos=kgit2/kommand&type=Date" />
   <img alt="Star History Chart" src="https://api.star-history.com/svg?repos=kgit2/kommand&type=Date" />
 </picture>
</a>
