# Windows 构建指南

## 理解 libstdc++-6.dll 问题

当在 Linux 上使用 `mingwX64` 目标构建 Kotlin Native 应用程序并在 Windows 上运行时，您可能会遇到如下错误：

```
libstdc++-6.dll is missing or not found
```

### 为什么会发生这种情况？

默认情况下，MinGW-w64 编译的二进制文件会动态链接到几个运行时库：
- `libgcc_s_seh-1.dll`（或类似）- GCC 运行时支持
- `libstdc++-6.dll` - C++ 标准库
- `libwinpthread-1.dll` - Windows 的 POSIX 线程实现

当您在 Linux 上使用 MinGW 交叉编译时，这些 DLL 文件不会包含在您的可执行文件中。当可执行文件在没有安装 MinGW 的 Windows 机器上运行时，Windows 无法找到这些所需的 DLL，从而导致错误。

### 解决方案

解决方案是将这些运行时库**静态链接**到您的可执行文件中。这意味着这些库的代码会直接嵌入到您的 `.exe` 文件中，消除了对单独 DLL 文件的需求。

## Kommand 如何处理这个问题

从 2.3.0 版本开始，Kommand 通过在 cinterop 定义文件中设置以下链接器选项，自动配置 Windows 构建的静态链接：

- `-static` - 尽可能静态链接所有库
- `-static-libgcc` - 静态链接 GCC 运行时库
- `-static-libstdc++` - 静态链接 C++ 标准库

这些标志配置在 `src/nativeInterop/cinterop/x86_64-pc-windows-gnu.def` 中。

## 如果我仍然遇到错误怎么办？

如果您使用的是旧版本的 Kommand 或从源代码构建，您可能需要：

1. **更新到最新版本**的 Kommand（2.3.0 或更高版本）

2. **使用正确的标志重新构建 kommand-core 本地库**：
   ```bash
   cd kommand-core
   just x86_64-pc-windows-gnu
   ```

3. **重新构建您的 Kotlin 应用程序**

## 对于应用程序开发者

当构建使用 Kommand 的 Kotlin Native 应用程序时，您不需要为 Kommand 本身添加任何特殊的链接器选项。但是，如果您的应用程序有其他本地依赖项，您可能需要在自己的 `build.gradle.kts` 中添加这些选项：

```kotlin
mingwX64() {
    binaries {
        executable {
            // 仅对您自己的本地依赖项需要，
            // 不适用于内部处理此问题的 Kommand
            linkerOpts.addAll(listOf("-static", "-static-libgcc", "-static-libstdc++"))
            entryPoint = "main"
        }
    }
}
```

## 在 Windows 上没有 MinGW 的情况下构建

**重要提示**：没有安装 MinGW-w64 工具链就无法构建 MinGW 二进制文件。这是一个基本要求：

- **在 Linux 上**：您需要 `mingw-w64` 交叉编译器（通常可通过包管理器获得）
- **在 macOS 上**：您需要 `mingw-w64`（可通过 Homebrew 安装：`brew install mingw-w64`）
- **在 Windows 上**：您需要 MinGW-w64 工具链（可通过 MSYS2 或独立安装程序获得）

静态链接标志解决了**运行时**依赖问题（用户不需要 MinGW 来*运行*可执行文件），但它们不能消除**构建时**依赖（您仍然需要 MinGW 来*构建*可执行文件）。

## 验证您的构建

使用静态链接构建后，您可以验证您的可执行文件不依赖于 MinGW DLL：

**在 Windows 上**，使用 `Dependencies` 工具或 `dumpbin`：
```cmd
dumpbin /dependents your-app.exe
```

**在 Linux 上**，使用 `objdump`：
```bash
x86_64-w64-mingw32-objdump -p your-app.exe | grep "DLL Name"
```

您应该只看到 Windows 系统 DLL（如 `KERNEL32.dll`、`msvcrt.dll`、`WS2_32.dll`），而**不应该**看到 `libstdc++-6.dll`、`libgcc_s_seh-1.dll` 或 `libwinpthread-1.dll`。

## 权衡取舍

### 静态链接的优点：
- ✅ 单个可执行文件 - 无 DLL 依赖
- ✅ 用户不需要安装 MinGW 即可运行您的应用程序
- ✅ 更容易部署和分发

### 静态链接的缺点：
- ❌ 更大的可执行文件大小（通常大 1-2 MB）
- ❌ 无法在多个应用程序之间共享库代码
- ❌ 构建时仍需要 MinGW 工具链

对于大多数用例，优点大于缺点，这就是为什么 Kommand 默认对 Windows 构建使用静态链接。
