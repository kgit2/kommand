package com.kgit2.process

import com.kgit2.io.PlatformReader
import com.kgit2.io.PlatformWriter
import com.kgit2.io.Reader
import com.kgit2.io.Writer
import io.ktor.utils.io.core.*
import io.ktor.utils.io.errors.*
import kotlinx.cinterop.Arena
import kotlinx.cinterop.CPointer
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
import kotlinx.cinterop.value
import platform.posix.FILE
import platform.posix._open_osfhandle
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
    actual fun start(options: ChildOptions) = memScoped {
        val saAttr = cValue<SECURITY_ATTRIBUTES>() {
            nLength = sizeOf<SECURITY_ATTRIBUTES>().convert()
            bInheritHandle = 1
            lpSecurityDescriptor = null
        }
        val pipes = createPipe(saAttr.ptr)
        // create child process
        val startupInformation = cValue<STARTUPINFO> {
            cb = sizeOf<STARTUPINFO>().convert()
            hStdInput = pipes.stdinPipeReaderHandle?.value
            hStdOutput = pipes.stdoutPipeWriterHandle?.value
            hStdError = pipes.stderrPipeWriterHandle?.value
            dwFlags = dwFlags or STARTF_USESTDHANDLES.convert()
        }
        val cmdLineString = listOf(command, *args.toTypedArray()).joinToString(" ")
        val cmdLine = allocArray<UShortVar>(cmdLineString.length.convert())
        cmdLineString.forEachIndexed { index, c ->
            cmdLine[index] = c.code.toUShort()
        }
        val success = CreateProcess!!.invoke(
            null,
            cmdLine,
            null,
            null,
            1,
            0u,
            null,
            null,
            startupInformation.ptr,
            processInformation.ptr
        )
        if (success == 0) {
            val errorCode = GetLastError()
            throw IOException("CreateProcess failed: $errorCode")
        }
        println("CreateProcess succeeded")
        CloseHandle(pipes.stdinPipeReaderHandle?.value)
        CloseHandle(pipes.stdoutPipeWriterHandle?.value)
        CloseHandle(pipes.stderrPipeWriterHandle?.value)
        WaitForSingleObject(processInformation.hProcess, INFINITE)
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
            WaitForSingleObject(processInformation.hProcess, INFINITE)
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

    private fun createPipe(saAttr: CPointer<SECURITY_ATTRIBUTES>): CreatePipeResult {
        val pipe = mutableListOf<Pair<IntArray, String>>()
        val result = CreatePipeResult()
        when (stdin) {
            Stdio.Pipe -> {
                result.stdinPipeReaderHandle = memory.alloc()
                result.stdinPipeWriterHandle = memory.alloc()
                // first param is read handle, second param is write handle
                CreatePipe(result.stdinPipeReaderHandle!!.ptr, result.stdinPipeWriterHandle!!.ptr, saAttr, 0)
                // set handle information for parent process
                SetHandleInformation(result.stdinPipeReaderHandle!!.value, HANDLE_FLAG_INHERIT, 0)
            }
            else -> Unit
        }
        when (stdout) {
            Stdio.Pipe -> {
                result.stdoutPipeReaderHandle = memory.alloc()
                result.stdoutPipeWriterHandle = memory.alloc()
                CreatePipe(result.stdoutPipeReaderHandle!!.ptr, result.stdoutPipeWriterHandle!!.ptr, saAttr, 0)
                // set handle information for parent process
                SetHandleInformation(result.stdoutPipeWriterHandle!!.value, HANDLE_FLAG_INHERIT, 0)
            }
            else -> Unit
        }
        when (stderr) {
            Stdio.Pipe -> {
                result.stderrPipeReaderHandle = memory.alloc()
                result.stderrPipeWriterHandle = memory.alloc()
                CreatePipe(result.stderrPipeReaderHandle!!.ptr, result.stderrPipeWriterHandle!!.ptr, saAttr, 0)
                // set handle information for parent process
                SetHandleInformation(result.stderrPipeWriterHandle!!.value, HANDLE_FLAG_INHERIT, 0)
            }
            else -> Unit
        }
        return result
    }

    private fun openFileDescriptor(pipes: CreatePipeResult) {
        if (pipes.stdinPipeWriterHandle != null) {
            val fd = _open_osfhandle(pipes.stdinPipeWriterHandle!!.reinterpret<intptr_tVar>().value, 0x0001)
            val file = fdopen(fd, "w")
            stdinWriter = Writer(PlatformWriter(file))
        }
        if (pipes.stdoutPipeReaderHandle != null) {
            val fd = _open_osfhandle(pipes.stdoutPipeReaderHandle!!.reinterpret<intptr_tVar>().value, 0x0000)
            val file = fdopen(fd, "r")
            stdoutReader = Reader(PlatformReader(file))
        }
        if (pipes.stderrPipeReaderHandle != null) {
            val fd = _open_osfhandle(pipes.stderrPipeReaderHandle!!.reinterpret<intptr_tVar>().value, 0x0000)
            val file = fdopen(fd, "r")
            val stderrReader = Reader(PlatformReader(file))
        }
    }

    @Throws(IOException::class)
    private fun fdopen(fileDescriptor: Int, mode: String): CPointer<FILE> {
        return when (val file = platform.posix.fdopen(fileDescriptor, mode)) {
            null -> throw IOException("Invalid mode.")
            else -> file
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
