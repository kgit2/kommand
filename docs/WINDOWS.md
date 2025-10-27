# Windows Build Guide

## Understanding the libstdc++-6.dll Issue

When building Kotlin Native applications with `mingwX64` target on Linux and running them on Windows, you may encounter an error like:

```
libstdc++-6.dll is missing or not found
```

### Why Does This Happen?

By default, MinGW-w64 compiled binaries dynamically link against several runtime libraries:
- `libgcc_s_seh-1.dll` (or similar) - GCC runtime support
- `libstdc++-6.dll` - C++ standard library
- `libwinpthread-1.dll` - POSIX threads implementation for Windows

When you cross-compile on Linux using MinGW, these DLL files are not included with your executable. When the executable runs on a Windows machine that doesn't have MinGW installed, Windows cannot find these required DLLs, resulting in the error.

### The Solution

The solution is to **statically link** these runtime libraries into your executable. This means the code from these libraries is embedded directly into your `.exe` file, eliminating the need for separate DLL files.

## How Kommand Handles This

Kommand automatically configures static linking for Windows builds by setting the following linker options in the cinterop definition file:

- `-static` - Link all libraries statically when possible
- `-static-libgcc` - Statically link the GCC runtime library
- `-static-libstdc++` - Statically link the C++ standard library

These flags are configured in `src/nativeInterop/cinterop/x86_64-pc-windows-gnu.def`.

## What If I Still Get the Error?

If you're building from source, you may need to:

1. **Rebuild the kommand-core native library** with the proper flags:
   ```bash
   cd kommand-core
   just x86_64-pc-windows-gnu
   ```

2. **Rebuild your Kotlin application**

## For Application Developers

When building your own Kotlin Native application that uses Kommand, you don't need to add any special linker options for Kommand itself. However, if your application has other native dependencies, you may need to add these options to your own `build.gradle.kts`:

```kotlin
mingwX64() {
    binaries {
        executable {
            // Only needed for YOUR OWN native dependencies,
            // not for Kommand which handles this internally
            linkerOpts.addAll(listOf("-static", "-static-libgcc", "-static-libstdc++"))
            entryPoint = "main"
        }
    }
}
```

## Building Without MinGW on Windows

**Important Note**: You cannot build MinGW binaries without having the MinGW-w64 toolchain installed. This is a fundamental requirement:

- **On Linux**: You need `mingw-w64` cross-compiler (usually available via package manager)
- **On macOS**: You need `mingw-w64` (installable via Homebrew: `brew install mingw-w64`)
- **On Windows**: You need MinGW-w64 toolchain (available via MSYS2 or standalone installers)

The static linking flags solve the **runtime** dependency issue (users don't need MinGW to *run* the executable), but they don't eliminate the **build-time** dependency (you still need MinGW to *build* the executable).

## Verifying Your Build

After building with static linking, you can verify that your executable doesn't depend on MinGW DLLs:

**On Windows**, use the `Dependencies` tool or `dumpbin`:
```cmd
dumpbin /dependents your-app.exe
```

**On Linux**, use `objdump`:
```bash
x86_64-w64-mingw32-objdump -p your-app.exe | grep "DLL Name"
```

You should see only Windows system DLLs (like `KERNEL32.dll`, `msvcrt.dll`, `WS2_32.dll`) and **not** see `libstdc++-6.dll`, `libgcc_s_seh-1.dll`, or `libwinpthread-1.dll`.

## Trade-offs

### Advantages of Static Linking:
- ✅ Single executable file - no DLL dependencies
- ✅ Users don't need MinGW installed to run your application
- ✅ Easier deployment and distribution

### Disadvantages of Static Linking:
- ❌ Larger executable size (typically 1-2 MB larger)
- ❌ Cannot share library code between multiple applications
- ❌ Still requires MinGW toolchain at build time

For most use cases, the advantages outweigh the disadvantages, which is why Kommand uses static linking by default for Windows builds.
