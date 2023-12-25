package com.kgit2.kommand

actual val platform: Platform
    get() {
        val os = kotlin.native.Platform.osFamily
        val arch = kotlin.native.Platform.cpuArchitecture
        return when (os) {
            OsFamily.MACOSX -> when (arch) {
                CpuArchitecture.ARM64 -> Platform.MACOS_ARM64
                CpuArchitecture.X64 -> Platform.MACOS_X64
                else -> throw Exception("Unsupported platform: $os $arch")
            }
            OsFamily.LINUX -> when (arch) {
                CpuArchitecture.ARM64 -> Platform.LINUX_ARM64
                CpuArchitecture.X64 -> Platform.LINUX_X64
                else -> throw Exception("Unsupported platform: $os $arch")
            }
            OsFamily.WINDOWS -> when (arch) {
                CpuArchitecture.X64 -> Platform.MINGW_X64
                else -> throw Exception("Unsupported platform: $os $arch")
            }
            else -> throw Exception("Unsupported platform: $os $arch")
        }
    }
