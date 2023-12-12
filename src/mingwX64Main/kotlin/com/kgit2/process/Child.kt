package com.kgit2.process

import com.kgit2.io.PlatformReader
import com.kgit2.io.PlatformWriter
import com.kgit2.io.Reader
import com.kgit2.io.Writer
import io.ktor.utils.io.core.*
import io.ktor.utils.io.errors.*
import kotlinx.cinterop.Arena
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CArrayPointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValue
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.UIntVar
import kotlinx.cinterop.UShortVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.cValue
import kotlinx.cinterop.convert
import kotlinx.cinterop.invoke
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.set
import kotlinx.cinterop.sizeOf
import kotlinx.cinterop.toKString
import kotlinx.cinterop.value
import platform.posix.FILE
import platform.posix._open_osfhandle
import platform.posix.fgets
import platform.posix.intptr_tVar
import platform.windows.CloseHandle
import platform.windows.CreatePipe
import platform.windows.CreateProcess
import platform.windows.GetExitCodeProcess
import platform.windows.GetLastError
import platform.windows.HANDLEVar
import platform.windows.HANDLE_FLAG_INHERIT
import platform.windows.INFINITE
import platform.windows.PROCESS_INFORMATION
import platform.windows.SECURITY_ATTRIBUTES
import platform.windows.STARTF_USESTDHANDLES
import platform.windows.STARTUPINFO
import platform.windows.SetHandleInformation
import platform.windows.WaitForSingleObject

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

    private var stdinWriter: Writer? = null
    private var stdoutReader: Reader? = null
    private var stderrReader: Reader? = null

    private val memory = Arena()
    private var processInformation = memory.alloc<PROCESS_INFORMATION>()

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
    actual fun start(options: ChildOptions): Unit = memScoped {
        val securityAttribute = createSecurityAttribute()
        val pipes = createPipe(securityAttribute.ptr)
        val startupInformation = createStartUpInformation(pipes)
        val cmdLine = createCMDLine(this)
        val cwd = createCurrentDirectory(this)
        // create child process
        val success = CreateProcess!!.invoke(
            null,
            cmdLine,
            null,
            null,
            1,
            0u,
            null,
            cwd,
            startupInformation.ptr,
            processInformation.ptr
        )
        if (success == 0) {
            val errorCode = GetLastError()
            throw IOException("CreateProcess failed: $errorCode")
        }
        redirectPipeHandle(pipes)
        id = processInformation.dwProcessId.toInt()
        openFileDescriptor(pipes)
    }

    @Throws(IOException::class)
    actual fun wait(): ChildExitStatus {
        stdinWriter?.close()
        WaitForSingleObject(processInformation.hProcess, INFINITE)
        val exitCode = memory.alloc<UIntVar>()
        GetExitCodeProcess(processInformation.hProcess, exitCode.ptr)
        stdoutReader?.close()
        stderrReader?.close()
        val status = ChildExitStatus(exitCode.value.toInt())
        CloseHandle(processInformation.hProcess)
        CloseHandle(processInformation.hThread)
        memory.clear()
        return status
    }

    @Throws(IOException::class)
    actual fun waitWithOutput(): String? {
        return if (stdout != Stdio.Pipe) {
            stdinWriter?.close()
            val exitCode = memory.alloc<UIntVar>()
            GetExitCodeProcess(processInformation.hProcess, exitCode.ptr)
            CloseHandle(processInformation.hProcess)
            CloseHandle(processInformation.hThread)
            memory.clear()
            null
        } else {
            stdinWriter?.close()
            val output = StringBuilder()
            val reader = stdoutReader!!
            while (!reader.endOfInput) {
                output.append(reader.readText())
            }
            WaitForSingleObject(processInformation.hProcess, INFINITE)
            stdoutReader?.close()
            stderrReader?.close()
            CloseHandle(processInformation.hProcess)
            CloseHandle(processInformation.hThread)
            memory.clear()
            output.toString()
        }
    }

    actual fun kill() {
    }

    actual fun prompt(): String {
        TODO("Not yet implemented")
    }

    private fun createSecurityAttribute(): CValue<SECURITY_ATTRIBUTES> {
        return cValue<SECURITY_ATTRIBUTES>() {
            nLength = sizeOf<SECURITY_ATTRIBUTES>().convert()
            bInheritHandle = 1
            lpSecurityDescriptor = null
        }
    }

    private fun createPipe(saAttr: CPointer<SECURITY_ATTRIBUTES>): CreatePipeResult {
        val pipes = CreatePipeResult()
        when (stdin) {
            Stdio.Pipe, Stdio.Null -> {
                pipes.stdinPipeReaderHandle = memory.alloc()
                pipes.stdinPipeWriterHandle = memory.alloc()
                // first param is read handle, second param is write handle
                CreatePipe(pipes.stdinPipeReaderHandle!!.ptr, pipes.stdinPipeWriterHandle!!.ptr, saAttr, 0u)
                // set handle information for parent process
                SetHandleInformation(pipes.stdinPipeWriterHandle!!.value, HANDLE_FLAG_INHERIT.convert(), 0u)
            }
            else -> Unit
        }
        when (stdout) {
            Stdio.Pipe, Stdio.Null -> {
                pipes.stdoutPipeReaderHandle = memory.alloc()
                pipes.stdoutPipeWriterHandle = memory.alloc()
                CreatePipe(pipes.stdoutPipeReaderHandle!!.ptr, pipes.stdoutPipeWriterHandle!!.ptr, saAttr, 0u)
                // set handle information for parent process
                SetHandleInformation(pipes.stdoutPipeReaderHandle!!.value, HANDLE_FLAG_INHERIT.convert(), 0u)
            }
            else -> Unit
        }
        when (stderr) {
            Stdio.Pipe, Stdio.Null -> {
                pipes.stderrPipeReaderHandle = memory.alloc()
                pipes.stderrPipeWriterHandle = memory.alloc()
                CreatePipe(pipes.stderrPipeReaderHandle!!.ptr, pipes.stderrPipeWriterHandle!!.ptr, saAttr, 0u)
                // set handle information for parent process
                SetHandleInformation(pipes.stderrPipeReaderHandle!!.value, HANDLE_FLAG_INHERIT.convert(), 0u)
            }
            else -> Unit
        }
        return pipes
    }

    private fun createStartUpInformation(pipes: CreatePipeResult): CValue<STARTUPINFO> {
        return cValue<STARTUPINFO> {
            cb = sizeOf<STARTUPINFO>().convert()
            pipes.stdinPipeReaderHandle?.let {
                hStdInput = it.value
            }
            pipes.stdoutPipeWriterHandle?.let {
                hStdOutput = it.value
            }
            pipes.stderrPipeWriterHandle?.let {
                hStdError = it.value
            }
            if (!(stdin == Stdio.Inherit && stdout == Stdio.Inherit && stderr == Stdio.Inherit)) {
                dwFlags = dwFlags or STARTF_USESTDHANDLES.convert()
            }
        }
    }

    private fun createCMDLine(memory: MemScope): CArrayPointer<UShortVar> {
        val cmdLineString = listOf(command, *args.toTypedArray()).joinToString(" ")
        val cmdLine = memory.allocArray<UShortVar>(cmdLineString.length.convert())
        cmdLineString.forEachIndexed { index, c ->
            cmdLine[index] = c.code.toUShort()
        }
        return cmdLine
    }

    private fun createCurrentDirectory(memory: MemScope): CPointer<UShortVar>? {
        return cwd?.let {
            val currentDirectory = memory.allocArray<UShortVar>(it.length.convert())
            it.forEachIndexed { index, c ->
                currentDirectory[index] = c.code.toUShort()
            }
            currentDirectory
        }
    }

    private fun redirectPipeHandle(pipes: CreatePipeResult) {
        when (stdin) {
            Stdio.Pipe, Stdio.Null -> CloseHandle(pipes.stdinPipeReaderHandle!!.value)
            Stdio.Inherit -> Unit
        }

        when (stdout) {
            Stdio.Pipe, Stdio.Null -> CloseHandle(pipes.stdoutPipeWriterHandle!!.value)
            Stdio.Inherit -> Unit
        }

        when (stderr) {
            Stdio.Pipe, Stdio.Null -> CloseHandle(pipes.stderrPipeWriterHandle!!.value)
            Stdio.Inherit -> Unit
        }
    }

    private fun openFileDescriptor(pipes: CreatePipeResult) {
        when (stdin) {
            Stdio.Pipe -> {
                if (pipes.stdinPipeWriterHandle != null) {
                    val fd = _open_osfhandle(pipes.stdinPipeWriterHandle!!.reinterpret<intptr_tVar>().value, 0x0001)
                    val file = fdopen(fd, "w")
                    stdinWriter = Writer(PlatformWriter(file))
                }
            }
            else -> Unit
        }

        when (stdout) {
            Stdio.Pipe -> {
                if (pipes.stdoutPipeReaderHandle != null) {
                    val fd = _open_osfhandle(pipes.stdoutPipeReaderHandle!!.reinterpret<intptr_tVar>().value, 0x0000)
                    val file = fdopen(fd, "r")
                    stdoutReader = Reader(PlatformReader(file))
                }
            }
            else -> Unit
        }

        when (stderr) {
            Stdio.Pipe -> {
                if (pipes.stderrPipeReaderHandle != null) {
                    val fd = _open_osfhandle(pipes.stderrPipeReaderHandle!!.reinterpret<intptr_tVar>().value, 0x0000)
                    val file = fdopen(fd, "r")
                    stderrReader = Reader(PlatformReader(file))
                }
            }
            else -> Unit
        }
    }

    @Throws(IOException::class)
    private fun fdopen(fileDescriptor: Int, mode: String): CPointer<FILE> {
        return when (val file = platform.posix.fdopen(fileDescriptor, mode)) {
            null -> throw IOException("Invalid mode.")
            else -> file
        }
    }

    // just for debug step by step
    private fun readFromFD(stdoutReader: HANDLEVar) {
        val fd = _open_osfhandle(stdoutReader.reinterpret<intptr_tVar>().value, 0)
        println("fd: $fd")
        val file = platform.posix.fdopen(fd, "r")
        memScoped {
            val buf = allocArray<ByteVar>(4096)
            while (true) {
                val result = fgets(buf, 4096, file)
                if (result != null) {
                    print(result.toKString())
                } else {
                    break
                }
            }
        }
    }
}

data class CreatePipeResult(
    // child process read from here for stdin
    var stdinPipeReaderHandle: HANDLEVar? = null,
    // parent process write to here for stdin
    var stdinPipeWriterHandle: HANDLEVar? = null,
    // parent process read from here for stdout
    var stdoutPipeReaderHandle: HANDLEVar? = null,
    // parent process write to here for stdout
    var stdoutPipeWriterHandle: HANDLEVar? = null,
    // parent process read from here for stderr
    var stderrPipeReaderHandle: HANDLEVar? = null,
    // parent process write to here for stderr
    var stderrPipeWriterHandle: HANDLEVar? = null,
)
