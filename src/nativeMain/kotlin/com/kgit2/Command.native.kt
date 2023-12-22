package com.kgit2

import kommand_core.*
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlin.native.ref.createCleaner

actual class Command(
    actual val command: String,
    private val inner: COpaquePointer?,
) {
    actual constructor(command: String) : this(command, new_command(command))

    private val cleaner = createCleaner(inner) {
        drop_command(inner)
    }

    override fun toString(): String {
        return display_command(inner)?.asString() ?: "null"
    }

    actual fun debugString(): String {
        return debug_command(inner)?.asString() ?: "null"
    }

    actual fun arg(arg: String): Command {
        arg_command(inner, arg)
        return this
    }

    actual fun args(args: List<String>): Command {
        for (arg in args) {
            arg_command(inner, arg)
        }
        return this
    }

    actual fun env(key: String, value: String): Command {
        env_command(inner, key, value)
        return this
    }

    actual fun envs(envs: Map<String, String>): Command {
        for ((key, value) in envs) {
            env_command(inner, key, value)
        }
        return this
    }

    actual fun removeEnv(key: String): Command {
        remove_env_command(inner, key)
        return this
    }

    actual fun envClear(): Command {
        env_clear_command(inner)
        return this
    }

    actual fun cwd(dir: String): Command {
        current_dir_command(inner, dir)
        return this
    }

    actual fun stdin(stdio: Stdio): Command {
        stdin_command(inner, stdio.to())
        return this
    }

    actual fun stdout(stdio: Stdio): Command {
        stdin_command(inner, stdio.to())
        return this
    }

    actual fun stderr(stdio: Stdio): Command {
        stdin_command(inner, stdio.to())
        return this
    }

    @Throws(KommandException::class)
    actual fun spawn(): Child {
        val result = spawn_command(inner)
        return run { Child.from(result) }
    }

    @Throws(KommandException::class)
    actual fun output(): Output {
        val result = output_command(inner)
        return run { Output.from(result) }
    }

    actual fun status(): Int? = memScoped {
        val result = status_command(inner)
        if (result.ptr.pointed.err != null) {
            val errPtr = result.ptr.pointed.err!!
            throw KommandException(errPtr.asString(), result.ptr.pointed.error_type.to())
        } else {
            result.ptr.pointed.ok
        }
    }
}

fun Stdio.to(): kommand_core.Stdio {
    return when (this) {
        Stdio.Inherit -> kommand_core.Stdio.Inherit
        Stdio.Pipe -> kommand_core.Stdio.Pipe
        Stdio.Null -> kommand_core.Stdio.Null
    }
}
