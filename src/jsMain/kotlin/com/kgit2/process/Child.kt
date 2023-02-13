package com.kgit2.process

import NodeJS.Dict
import NodeJS.ProcessEnv
import child_process.ChildProcess
import child_process.SpawnOptions
import child_process.SpawnSyncOptions
import child_process.spawn
import child_process.spawnSync
import com.kgit2.io.Reader
import com.kgit2.io.Writer
import io.ktor.utils.io.errors.*

val module = js("require('child_process')")

actual class Child actual constructor(
    actual val command: String,
    actual val args: List<String>,
    actual val envs: Map<String, String>,
    actual val cwd: String?,
    actual val stdin: Stdio,
    actual val stdout: Stdio,
    actual val stderr: Stdio
) {
    actual var id: Int? = null

    actual fun getChildStdin(): Writer? {
        TODO("Not yet implemented")
    }

    actual fun getChildStdout(): Reader? {
        TODO("Not yet implemented")
    }

    actual fun getChildStderr(): Reader? {
        TODO("Not yet implemented")
    }

    @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
    actual fun start(options: ChildOptions) {
        val spawnOptions: SpawnOptions = js("{}") as SpawnOptions
        spawnOptions.cwd = cwd
        spawnOptions.env = envs.asDynamic() as ProcessEnv
        spawnOptions.stdio = arrayOf(stdin, stdout, stderr).map {
            when (it) {
                Stdio.Inherit -> "inherit"
                Stdio.Pipe -> "pipe"
                Stdio.Null -> "ignore"
            }
        }.toTypedArray()
        val process = spawn(command, spawnOptions)
        this.id = process.pid.toInt()
        process.stdin
    }

    actual fun wait(): ChildExitStatus {
        return ChildExitStatus(0)
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
