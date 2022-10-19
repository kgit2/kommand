package com.floater.process

import com.floater.io.PlatformReader
import com.floater.io.PlatformWriter
import com.floater.io.Reader
import com.floater.io.Writer
import com.floater.process.Stdio.*
import io.ktor.utils.io.core.*
import kotlinx.cinterop.*
import platform.posix.*

const val READ_END = 0
const val WRITE_END = 1

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

    private var stdinWriter: Writer? = null
    private var stdoutReader: Reader? = null
    private var stderrReader: Reader? = null

    private val stdinPipe = IntArray(2)
    private val stdoutPipe = IntArray(2)
    private val stderrPipe = IntArray(2)

    private var options = ChildOptions.W_UNTRACED

    actual fun getChildStdin(): Writer? {
        return when (stdin) {
            Inherit, Null -> null
            Pipe -> stdinWriter
        }
    }

    actual fun getChildStdout(): Reader? {
        return when (stdout) {
            Inherit, Null -> null
            Pipe -> stdoutReader
        }
    }

    actual fun getChildStderr(): Reader? {
        return when (stderr) {
            Inherit, Null -> null
            Pipe -> stderrReader
        }
    }

    actual fun start(options: ChildOptions) {
        this.options = options
        memScoped {
            createPipe()
            val childPid = fork()
            processChild(childPid, this)
            processLocal(childPid, this)
        }
    }

    actual fun wait(): ChildExitStatus {
        return memScoped {
            val statusCode = alloc<IntVar>()
            waitpid(id!!, statusCode.ptr, options.value)
            ChildExitStatus(statusCode.value)
        }
    }

    actual fun waitWithOutput(): String? {
        return if (stdout != Pipe) {
            wait()
            null
        } else {
            val output = StringBuilder()
            val reader = stdoutReader!!
            while (reader.canRead()) {
                output.append(reader.readText())
            }
            output.toString()
        }
    }

    actual fun kill() {
        assert(id != null)
        kill(id!!, SIGTERM)
    }

    private fun processChild(childPid: Int, memScope: MemScope) {
        if (childPid == 0) {
            redirectFileDescriptor()
            val commands = listOf(command, *args.toTypedArray())
            execvp(commands[0], memScope.allocArrayOf(commands.map { it.cstr.getPointer(memScope) }))
        }
    }

    private fun processLocal(childPid: Int, memScope: MemScope) {
        if (childPid > 0) {
            this@Child.id = childPid
            val (stdinFile, stdoutFile, stderrFile) = openFileDescriptor()
            if (stdinFile != null) {
                this.stdinWriter = createWriter(stdinFile)
            }
            if (stdoutFile != null) {
                this.stdoutReader = createReader(stdoutFile)
            }
            if (stderrFile != null) {
                this.stderrReader = createReader(stderrFile)
            }
        }
    }

    private fun createPipe() {
        when (stdin) {
            Pipe -> stdinPipe.usePinned {
                pipe(it.addressOf(0))
            }

            else -> Unit
        }
        when (stdout) {
            Pipe -> stdoutPipe.usePinned {
                pipe(it.addressOf(0))
            }

            else -> Unit
        }
        when (stderr) {
            Pipe -> stderrPipe.usePinned {
                pipe(it.addressOf(0))
            }

            else -> Unit
        }
    }

    private fun redirectFileDescriptor() {
        when (stdin) {
            Null -> {
                close(STDIN_FILENO)
            }
            Pipe -> {
                dup2(stdinPipe[READ_END], STDIN_FILENO)
                close(stdinPipe[WRITE_END])
            }
            Inherit -> Unit
        }
        when (stdout) {
            Null -> {
                close(STDOUT_FILENO)
            }
            Pipe -> {
                dup2(stdoutPipe[WRITE_END], STDOUT_FILENO)
                close(stdoutPipe[READ_END])
            }

            Inherit -> Unit
        }
        when (stderr) {
            Null -> {
                close(STDERR_FILENO)
            }
            Pipe -> {
                dup2(stderrPipe[WRITE_END], STDERR_FILENO)
                close(stderrPipe[READ_END])
            }
            Inherit -> Unit
        }
    }

    private fun openFileDescriptor(): Triple<CPointer<FILE>?, CPointer<FILE>?, CPointer<FILE>?> {
        val stdinFile = when (stdin) {
            Pipe -> {
                close(stdinPipe[READ_END])
                fdopen(stdinPipe[WRITE_END], "w")
            }

            else -> null
        }
        val stdoutFile = when (stdout) {
            Pipe -> {
                close(stdoutPipe[WRITE_END])
                fdopen(stdoutPipe[READ_END], "r")
            }

            else -> null
        }
        val stderrFile = when (stderr) {
            Pipe -> {
                close(stderrPipe[WRITE_END])
                fdopen(stderrPipe[READ_END], "r")
            }

            else -> null
        }
        return Triple(stdinFile, stdoutFile, stderrFile)
    }

    companion object {
        private fun createWriter(file: CPointer<FILE>): Writer {
            return Writer(PlatformWriter(file))
        }

        private fun createReader(file: CPointer<FILE>): Reader {
            return Reader(PlatformReader(file))
        }

        private fun readFromFile(file: CPointer<FILE>, writer: BytePacketBuilder, memScope: MemScope) {
            val buffer = memScope.allocPointerTo<ByteVar>()
            while (true) {
                val size = getline(buffer.ptr, cValue(), file)
                if (size <= 0L) break
                for (i in 0 until size) {
                    writer.writeByte(buffer.value!![i])
                }
            }
            fclose(file)
        }
    }
}
