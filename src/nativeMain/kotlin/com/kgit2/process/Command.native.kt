package com.kgit2.process

import com.kgit2.exception.KommandException
import com.kgit2.io.Output
import com.kgit2.wrapper.*
import kotlinx.cinterop.COpaquePointer
import kotlin.native.ref.createCleaner

actual class Command(
    actual val command: String,
    private val inner: COpaquePointer?,
) {
    actual constructor(command: String) : this(command, newCommand(command))

    private val cleaner = createCleaner(inner) { command ->
        dropCommand(command)
    }

    override fun toString(): String {
        return displayCommand(inner) ?: "null"
    }

    actual fun debugString(): String {
        return debugCommand(inner) ?: "null"
    }

    actual fun arg(arg: String): Command {
        argCommand(inner, arg)
        return this
    }

    actual fun args(args: List<String>): Command {
        for (arg in args) {
            argCommand(inner, arg)
        }
        return this
    }

    actual fun env(key: String, value: String): Command {
        envCommand(inner, key, value)
        return this
    }

    actual fun envs(envs: Map<String, String>): Command {
        for ((key, value) in envs) {
            envCommand(inner, key, value)
        }
        return this
    }

    actual fun removeEnv(key: String): Command {
        removeEnvCommand(inner, key)
        return this
    }

    actual fun envClear(): Command {
        envClearCommand(inner)
        return this
    }

    actual fun cwd(dir: String): Command {
        currentDirCommand(inner, dir)
        return this
    }

    actual fun stdin(stdio: Stdio): Command {
        stdinCommand(inner, stdio)
        return this
    }

    actual fun stdout(stdio: Stdio): Command {
        stdoutCommand(inner, stdio)
        return this
    }

    actual fun stderr(stdio: Stdio): Command {
        stderrCommand(inner, stdio)
        return this
    }

    @Throws(KommandException::class)
    actual fun spawn(): Child = run {
        spawnCommand(inner)
    }

    @Throws(KommandException::class)
    actual fun output(): Output = run {
        outputCommand(inner)
    }

    @Throws(KommandException::class)
    actual fun status(): Int = run {
        statusCommand(inner)
    }
}
