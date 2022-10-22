package com.floater.process

import com.floater.io.PlatformReader
import com.floater.io.PlatformWriter
import com.floater.io.Reader
import com.floater.io.Writer
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

    actual fun start(options: ChildOptions) {
        val processBuilder = ProcessBuilder(command, *args.toTypedArray())
        cwd?.let { processBuilder.directory(File(it)) }
        redirectStdio(processBuilder)
        process = processBuilder.start()
        this.id = process?.pid()?.toInt()
        this.stdinWriter = createWriter(stdin, process!!)
        this.stdoutReader = createReader(stdout, process!!)
        this.stderrReader = createReader(stderr, process!!)
            // .directory(cwd?.let { File(it) })

    }

    actual fun wait(): ChildExitStatus {
        val exitCode = try {
            process!!.waitFor()
        } catch (e: InterruptedException) {
            e.printStackTrace()
            0x7F
        }
        return ChildExitStatus(exitCode)
    }

    actual fun waitWithOutput(): String? {
        wait()
        return stdoutReader?.readText()
    }

    actual fun kill() {
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
