package com.kgit2.kommand

actual val platform: Platform
    get() {
        val os = System.getProperty("os.name")
        val arch = System.getProperty("os.arch")
        return when {
            os.contains("Mac OS X") -> {
                when {
                    arch.contains("aarch64") -> Platform.MACOS_ARM64
                    else -> Platform.MACOS_X64
                }
            }
            os.contains("Linux") -> {
                when {
                    arch.contains("aarch64") -> Platform.LINUX_ARM64
                    else -> Platform.LINUX_X64
                }
            }
            os.contains("Windows") -> Platform.MINGW_X64
            else -> throw Exception("Unsupported platform: $os $arch")
        }
    }
