package com.kgit2

import com.kgit2.process.Command
import kotlin.test.Test

class PlatformTest {
    @Test
    fun platform() {
        platform
    }
}

fun platformEchoPath(): String {
    return when (platform) {
        Platform.MACOS_X64 -> "kommand-core/target/x86_64-apple-darwin/release/kommand-echo"
        Platform.MACOS_ARM64 -> "kommand-core/target/aarch64-apple-darwin/release/kommand-echo"
        Platform.LINUX_X64 -> "kommand-core/target/x86_64-unknown-linux-gnu/release/kommand-echo"
        Platform.LINUX_ARM64 -> "kommand-core/target/aarch64-unknown-linux-gnu/release/kommand-echo"
        Platform.MINGW_X64 -> "kommand-core/target/x86_64-pc-windows-gnu/release/kommand-echo"
    }
}

fun platformCwd(): Command {
    return when (platform) {
        Platform.MACOS_X64 -> Command("pwd")
        Platform.MACOS_ARM64 -> Command("pwd")
        Platform.LINUX_X64 -> Command("pwd")
        Platform.LINUX_ARM64 -> Command("pwd")
        Platform.MINGW_X64 -> Command("cmd").arg("/c").arg("chdir")
    }
}

fun homeDir(): String {
    return when (platform) {
        Platform.MACOS_X64 -> com.kgit2.env.envVar("HOME")!!
        Platform.MACOS_ARM64 -> com.kgit2.env.envVar("HOME")!!
        Platform.LINUX_X64 -> com.kgit2.env.envVar("HOME")!!
        Platform.LINUX_ARM64 -> com.kgit2.env.envVar("HOME")!!
        Platform.MINGW_X64 -> com.kgit2.env.envVar("userprofile")!!
    }
}
