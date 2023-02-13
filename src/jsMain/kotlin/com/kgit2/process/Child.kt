package com.kgit2.process

import com.kgit2.io.Reader
import com.kgit2.io.Writer
import io.ktor.utils.io.errors.*

actual class Child actual constructor(
    command: String,
    args: List<String>,
    envs: Map<String, String>,
    cwd: String?,
    stdin: Stdio,
    stdout: Stdio,
    stderr: Stdio
) {
    actual val command: String
        get() = TODO("Not yet implemented")
    actual val args: List<String>
        get() = TODO("Not yet implemented")
    actual val envs: Map<String, String>
        get() = TODO("Not yet implemented")
    actual val cwd: String?
        get() = TODO("Not yet implemented")
    actual val stdin: Stdio
        get() = TODO("Not yet implemented")
    actual val stdout: Stdio
        get() = TODO("Not yet implemented")
    actual val stderr: Stdio
        get() = TODO("Not yet implemented")
    actual var id: Int?
        get() = TODO("Not yet implemented")
        set(value) {}

    actual fun getChildStdin(): Writer? {
        TODO("Not yet implemented")
    }

    actual fun getChildStdout(): Reader? {
        TODO("Not yet implemented")
    }

    actual fun getChildStderr(): Reader? {
        TODO("Not yet implemented")
    }

    actual fun start(options: ChildOptions) {
    }

    actual fun wait(): ChildExitStatus {
        TODO("Not yet implemented")
    }

    actual fun waitWithOutput(): String? {
        TODO("Not yet implemented")
    }

    actual fun kill() {
    }

    actual fun prompt(): String {
        TODO("Not yet implemented")
    }

}
