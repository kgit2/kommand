package com.floater.process

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

    private val stdinWriter: BytePacketBuilder = BytePacketBuilder()
    private val stdoutWriter: BytePacketBuilder = BytePacketBuilder()
    private val stderrWriter: BytePacketBuilder = BytePacketBuilder()

    private val stdinPipe = IntArray(2)
    private val stdoutPipe = IntArray(2)
    private val stderrPipe = IntArray(2)

    private var options = ChildOptions.W_UNTRACED

    init {
        stdinWriter.appendLine("hello world")
        stdinWriter.appendLine("0")
        stdinWriter.appendLine("1")
        stdinWriter.appendLine("2")
        stdinWriter.appendLine("3")
    }

    actual fun getChildStdin(): BytePacketBuilder? {
        return when (stdin) {
            Inherit, Null -> null
            Pipe -> stdinWriter
        }
    }

    actual fun getChildStdout(): ByteReadPacket? {
        return when (stdout) {
            Inherit, Null -> null
            Pipe -> stdoutWriter.build()
        }
    }

    actual fun getChildStderr(): ByteReadPacket? {
        return when (stderr) {
            Inherit, Null -> null
            Pipe -> stderrWriter.build()
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

    actual fun waitWithOutput(): ByteReadPacket? {
        wait()
        if (stdout != Pipe) {
            return null
        }
        return stdoutWriter.build()
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
                writeToFile(stdinFile, stdinWriter.build(), memScope)
            }
            if (stdoutFile != null) {
                readFromFile(stdoutFile, stdoutWriter, memScope)
            }
            if (stderrFile != null) {
                readFromFile(stderrFile, stderrWriter, memScope)
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
        private fun writeToFile(file: CPointer<FILE>, reader: ByteReadPacket, memScope: MemScope) {
            println("reader.canRead() = ${reader.canRead()}")
            while (reader.canRead()) {
                fprintf(file, "%s\n", reader.readUTF8Line())
            }
            fclose(file)
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
