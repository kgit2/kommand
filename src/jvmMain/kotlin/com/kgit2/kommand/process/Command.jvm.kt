package com.kgit2.kommand.process

import com.kgit2.kommand.exception.KommandException
import com.kgit2.kommand.io.Output
import java.io.File

actual class Command(
    actual val command: String,
    private val builder: ProcessBuilder,
) {

    actual constructor(command: String) : this(command, ProcessBuilder(command))

    actual fun debugString(): String {
        return "Command(command='$command', builder=${
            builder.command()
                .toMutableList()
                .apply { this.removeFirst() }
                .joinToString(",", "[", "]")
        })"
    }

    actual fun arg(arg: String): Command {
        builder.command().add(arg)
        return this
    }

    actual fun args(args: List<String>): Command {
        builder.command().addAll(args)
        return this
    }

    actual fun env(key: String, value: String): Command {
        builder.environment()[key] = value
        return this
    }

    actual fun envs(envs: Map<String, String>): Command {
        builder.environment().putAll(envs)
        return this
    }

    actual fun removeEnv(key: String): Command {
        builder.environment().remove(key)
        return this
    }

    actual fun envClear(): Command {
        builder.environment().clear()
        return this
    }

    actual fun cwd(dir: String): Command {
        builder.directory(File(dir))
        return this
    }

    actual fun stdin(stdio: Stdio): Command {
        builder.redirectInput(stdio.to())
        return this
    }

    actual fun stdout(stdio: Stdio): Command {
        builder.redirectOutput(stdio.to())
        return this
    }

    actual fun stderr(stdio: Stdio): Command {
        builder.redirectError(stdio.to())
        return this
    }

    @Throws(KommandException::class)
    actual fun spawn(): Child {
        val process = builder.start()
        return Child(process)
    }

    @Throws(KommandException::class)
    actual fun output(): Output {
        val process = builder.start()
        val stdoutContent = process.inputReader().readText()
        val stderrContent = process.errorReader().readText()
        val status = process.waitFor()
        return Output(status, stdoutContent, stderrContent)
    }

    @Throws(KommandException::class)
    actual fun status(): Int {
        return builder.start().waitFor()
    }
}

fun Stdio.to(): ProcessBuilder.Redirect {
    return when (this) {
        Stdio.Inherit -> ProcessBuilder.Redirect.INHERIT
        Stdio.Null -> ProcessBuilder.Redirect.DISCARD
        Stdio.Pipe -> ProcessBuilder.Redirect.PIPE
    }
}
