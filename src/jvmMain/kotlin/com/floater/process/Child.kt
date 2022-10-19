package com.floater.process

import io.ktor.utils.io.core.*

actual class Child actual constructor(
    actual val command: String,
    actual val args: List<String>,
    actual val envs: Map<String, String>,
    actual val cwd: String?,
    actual val stdin: Stdio,
    actual val stdout: Stdio,
    actual val stderr: Stdio,
) {
    actual var id: Int? = null

    actual fun getChildStdin(): BytePacketBuilder? {
        TODO("Not yet implemented")
    }

    actual fun getChildStdout(): ByteReadPacket? {
        TODO("Not yet implemented")
    }

    actual fun getChildStderr(): ByteReadPacket? {
        TODO("Not yet implemented")
    }

    actual fun start(options: ChildOptions) {
    }

    actual fun wait(): ChildExitStatus {
        TODO("Not yet implemented")
    }

    actual fun waitWithOutput(): ByteReadPacket? {
        TODO("Not yet implemented")
    }

    actual fun kill() {
    }
}
