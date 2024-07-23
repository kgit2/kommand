package com.kgit2.kommand.process

import com.kgit2.kommand.asString
import com.kgit2.kommand.exception.KommandException
import com.kgit2.kommand.from
import com.kgit2.kommand.io.Output
import com.kgit2.kommand.to
import kommand_core.arg_command
import kommand_core.current_dir_command
import kommand_core.display_command
import kommand_core.drop_command
import kommand_core.env_clear_command
import kommand_core.env_command
import kommand_core.new_command
import kommand_core.output_command
import kommand_core.remove_env_command
import kommand_core.spawn_command
import kommand_core.status_command
import kommand_core.stderr_command
import kommand_core.stdin_command
import kommand_core.stdout_command
import kotlinx.cinterop.COpaquePointer
import kotlin.native.ref.createCleaner

actual class Command(
    actual val command: String,
    private val inner: COpaquePointer?,
) {
    actual constructor(command: String) : this(command, new_command(command))

    private val cleaner = createCleaner(inner) { command ->
        drop_command(command)
    }

    override fun toString(): String {
        return display_command(inner)?.asString() ?: "null"
    }

    actual fun debugString(): String {
        return display_command(inner)?.asString() ?: "null"
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

    actual fun args(vararg args: String): Command {
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

    actual fun envs(vararg envs: Pair<String, String>): Command {
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
        stdout_command(inner, stdio.to())
        return this
    }

    actual fun stderr(stdio: Stdio): Command {
        stderr_command(inner, stdio.to())
        return this
    }

    @Throws(KommandException::class)
    actual fun spawn(): Child = run {
        Child.from(spawn_command(inner))
    }

    @Throws(KommandException::class)
    actual fun output(): Output = run {
        Output.from(output_command(inner))
    }

    @Throws(KommandException::class)
    actual fun status(): Int = run {
        Int.from(status_command(inner))
    }
}
