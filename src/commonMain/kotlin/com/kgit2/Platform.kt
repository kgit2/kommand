package com.kgit2

enum class Platform {
    MACOS_X64,
    MACOS_ARM64,
    LINUX_X64,
    LINUX_ARM64,
    WINDOWS_X64,
    JVM
}

expect val platform: Platform
