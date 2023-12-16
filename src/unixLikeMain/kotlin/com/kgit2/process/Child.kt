package com.kgit2.process

import com.kgit2.io.Reader
import com.kgit2.io.Writer
import com.kgit2.process.Stdio.Inherit
import com.kgit2.process.Stdio.Null
import com.kgit2.process.Stdio.Pipe
import io.ktor.utils.io.errors.*
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.memScoped
import platform.posix.FILE
import platform.posix.SIGTERM
import platform.posix.STDERR_FILENO
import platform.posix.STDIN_FILENO
import platform.posix.STDOUT_FILENO

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

    @Throws(IOException::class)
    actual fun start(options: ChildOptions) {
        this.options = options
        memScoped {
            createPipe()
            val childPid = Posix.fork()
            processChild(childPid)
            processLocal(childPid)
        }
    }

    @Throws(IOException::class)
    actual fun wait(): ChildExitStatus {
        stdinWriter?.close()
        val status = Posix.waitpid(id!!, options.value)
        stdoutReader?.close()
        stderrReader?.close()
        return status
    }

    @Throws(IOException::class)
    actual fun waitWithOutput(): String? {
        return if (stdout != Pipe) {
            stdinWriter?.close()
            Posix.waitpid(id!!, options.value)
            null
        } else {
            stdinWriter?.close()
            Posix.waitpid(id!!, options.value)
            val output = StringBuilder()
            val reader = stdoutReader!!
            while (!reader.endOfInput) {
                output.append(reader.readText())
            }
            stdoutReader?.close()
            stderrReader?.close()
            output.toString()
        }
    }

    @Throws(IOException::class)
    actual fun kill() {
        assert(id != null)
        Posix.kill(id!!, SIGTERM)
    }

    @Throws(IOException::class)
    private fun processChild(childPid: Int) {
        if (childPid == 0) {
            redirectFileDescriptor()
            if (cwd != null) {
                Posix.chdir(cwd)
            }
            val commands = listOf(command, *args.toTypedArray(), null)
            Posix.execvp(commands)
        }
    }

    private fun processLocal(childPid: Int) {
        if (childPid > 0) {
            this@Child.id = childPid
            val (stdinFile, stdoutFile, stderrFile) = openFileDescriptor()
            if (stdinFile != null) {
                this.stdinWriter = Posix.createWriter(stdinFile)
            }
            if (stdoutFile != null) {
                this.stdoutReader = Posix.createReader(stdoutFile)
            }
            if (stderrFile != null) {
                this.stderrReader = Posix.createReader(stderrFile)
            }
        }
    }

    private fun createPipe() {
        val pipes = mutableListOf<Pair<IntArray, String>>()
        when (stdin) {
            Pipe -> stdinPipe
            else -> null
        }?.also { pipes.add(it to "stdin") }
        when (stdout) {
            Pipe -> stdoutPipe
            else -> null
        }?.also { pipes.add(it to "stdout") }
        when (stderr) {
            Pipe -> stderrPipe
            else -> null
        }?.also { pipes.add(it to "stderr") }
        pipes.forEach {
            Posix.pipe(it.first)
        }
    }

    private fun redirectFileDescriptor() {
        when (stdin) {
            Null -> {
                Posix.close(STDIN_FILENO)
            }

            Pipe -> {
                Posix.dup2(stdinPipe[READ_END], STDIN_FILENO)
                Posix.close(stdinPipe[WRITE_END])
            }

            Inherit -> Unit
        }
        when (stdout) {
            Null -> {
                Posix.close(STDOUT_FILENO)
            }

            Pipe -> {
                Posix.dup2(stdoutPipe[WRITE_END], STDOUT_FILENO)
                Posix.close(stdoutPipe[READ_END])
            }

            Inherit -> Unit
        }
        when (stderr) {
            Null -> {
                Posix.close(STDERR_FILENO)
            }

            Pipe -> {
                Posix.dup2(stderrPipe[WRITE_END], STDERR_FILENO)
                Posix.close(stderrPipe[READ_END])
            }

            Inherit -> Unit
        }
    }

    @Throws(IOException::class)
    private fun openFileDescriptor(): Triple<CPointer<FILE>?, CPointer<FILE>?, CPointer<FILE>?> {
        val stdinFile = when (stdin) {
            Pipe -> {
                Posix.close(stdinPipe[READ_END])
                Posix.fdopen(stdinPipe[WRITE_END], "w")
            }

            else -> null
        }
        val stdoutFile = when (stdout) {
            Pipe -> {
                Posix.close(stdoutPipe[WRITE_END])
                Posix.fdopen(stdoutPipe[READ_END], "r")
            }

            else -> null
        }
        val stderrFile = when (stderr) {
            Pipe -> {
                Posix.close(stderrPipe[WRITE_END])
                Posix.fdopen(stderrPipe[READ_END], "r")
            }

            else -> null
        }
        return Triple(stdinFile, stdoutFile, stderrFile)
    }

    override fun toString(): String {
        return "Child(command='$command', args=$args, envs=$envs, cwd=$cwd, stdin=$stdin, stdout=$stdout, stderr=$stderr, id=$id, stdinPipe=${stdinPipe.contentToString()}, stdoutPipe=${stdoutPipe.contentToString()}, stderrPipe=${stderrPipe.contentToString()}, options=$options)"
    }

    actual fun prompt(): String {
        return "$command ${args.joinToString(" ")}"
    }
}
