package com.kgit2.process

import com.kgit2.io.PlatformReader
import com.kgit2.io.PlatformWriter
import com.kgit2.io.Reader
import com.kgit2.io.Writer
import io.ktor.utils.io.errors.*
import java.io.File

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

    private var process: Process? = null

    private var stdinWriter: Writer? = null
    private var stdoutReader: Reader? = null
    private var stderrReader: Reader? = null

    actual fun getChildStdin(): Writer? {
        return when (stdin) {
            Stdio.Inherit, Stdio.Null -> null
            Stdio.Pipe -> stdinWriter
        }
    }

    actual fun getChildStdout(): Reader? {
        return when (stdout) {
            Stdio.Inherit, Stdio.Null -> null
            Stdio.Pipe -> stdoutReader
        }
    }

    actual fun getChildStderr(): Reader? {
        return when (stderr) {
            Stdio.Inherit, Stdio.Null -> null
            Stdio.Pipe -> stderrReader
        }
    }

    @Throws(IOException::class)
    actual fun start(options: ChildOptions) {
        val processBuilder = ProcessBuilder(command, *args.toTypedArray())
        cwd?.let { processBuilder.directory(File(it)) }
        redirectStdio(processBuilder)
        runCatching {
            process = processBuilder.start()
        }.onFailure {
            throw IOException(it)
        }
        this.id = process?.pid()?.toInt()
        this.stdinWriter = createWriter(stdin, process!!)
        this.stdoutReader = createReader(stdout, process!!)
        this.stderrReader = createReader(stderr, process!!)
    }

    @Throws(IOException::class)
    actual fun wait(): ChildExitStatus {
        val exitCode = try {
            stdinWriter?.close()
            val exitCode = process!!.waitFor()
            stdoutReader?.close()
            stderrReader?.close()
            exitCode
        } catch (e: InterruptedException) {
            0x7F
        } catch (e: java.io.IOException) {
            throw IOException(e)
        }
        return ChildExitStatus(exitCode)
    }

    @Throws(IOException::class)
    actual fun waitWithOutput(): String? {
        return if (stdout != Stdio.Pipe) {
            stdinWriter?.close()
            process!!.waitFor()
            stderrReader?.close()
            null
        } else {
            stdinWriter?.close()
            val output = StringBuilder()
            val reader = stdoutReader!!
            while (!reader.endOfInput) {
                output.append(reader.readText())
            }
            process!!.waitFor()
            stdoutReader?.close()
            stderrReader?.close()
            output.toString()
        }
    }

    actual fun kill() {
        process?.destroy()
    }

    private fun redirectStdio(processBuilder: ProcessBuilder) {
        when (stdin) {
            Stdio.Inherit -> {
                processBuilder.redirectInput(ProcessBuilder.Redirect.INHERIT)
            }

            Stdio.Null -> {
                processBuilder.redirectInput(ProcessBuilder.Redirect.DISCARD)
            }

            Stdio.Pipe -> {
                processBuilder.redirectInput(ProcessBuilder.Redirect.PIPE)
            }
        }
        when (stdout) {
            Stdio.Inherit -> {
                processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT)
            }

            Stdio.Null -> {
                processBuilder.redirectOutput(ProcessBuilder.Redirect.DISCARD)
            }

            Stdio.Pipe -> {
                processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE)
            }
        }
        when (stderr) {
            Stdio.Inherit -> {
                processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT)
            }

            Stdio.Null -> {
                processBuilder.redirectError(ProcessBuilder.Redirect.DISCARD)
            }

            Stdio.Pipe -> {
                processBuilder.redirectError(ProcessBuilder.Redirect.PIPE)
            }
        }
    }

    override fun toString(): String {
        return "Child(command='$command', args=$args, envs=$envs, cwd=$cwd, stdin=$stdin, stdout=$stdout, stderr=$stderr, id=$id, process=$process)"
    }

    actual fun prompt(): String {
        return "$command ${args.joinToString(" ")}"
    }

    companion object {
        private fun createWriter(stdio: Stdio, process: Process): Writer? {
            return when (stdio) {
                Stdio.Inherit, Stdio.Null -> null
                Stdio.Pipe -> {
                    val outputStream = process.outputStream
                    Writer(PlatformWriter(outputStream))
                }
            }
        }

        private fun createReader(stdio: Stdio, process: Process): Reader? {
            return when (stdio) {
                Stdio.Inherit, Stdio.Null -> null
                Stdio.Pipe -> {
                    val inputStream = process.inputStream
                    Reader(PlatformReader(inputStream))
                }
            }
        }
    }


}
