package com.kgit2.kommand.process

import com.kgit2.kommand.exception.ErrorType
import com.kgit2.kommand.exception.KommandException
import com.kgit2.kommand.io.BufferedReader
import com.kgit2.kommand.io.BufferedWriter
import com.kgit2.kommand.io.Output
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

actual class Child(
    private val process: Process,
) {
    private var stdin: AtomicReference<BufferedWriter?> = AtomicReference(null)
    private var stdout: AtomicReference<BufferedReader?> = AtomicReference(null)
    private var stderr: AtomicReference<BufferedReader?> = AtomicReference(null)

    actual fun id(): UInt {
        return process.pid().toUInt()
    }

    actual fun bufferedStdin(): BufferedWriter? {
        stdin.compareAndSet(null, BufferedWriter(process.outputWriter()))
        return stdin.get()
    }

    actual fun bufferedStdout(): BufferedReader? {
        stdout.compareAndSet(null, BufferedReader(process.inputReader()))
        return stdout.get()
    }

    actual fun bufferedStderr(): BufferedReader? {
        stderr.compareAndSet(null, BufferedReader(process.errorReader()))
        return stderr.get()
    }

    @Throws(KommandException::class)
    actual fun kill() {
        stdin.get()?.close()
        process.destroy()
    }

    @Throws(KommandException::class)
    actual fun wait(): Int {
        stdin.get()?.close()
        return process.waitFor()
    }

    @Throws(KommandException::class)
    actual fun tryWait(): Int? {
        return when (process.waitFor(0, TimeUnit.MICROSECONDS)) {
            true -> wait()
            false -> return null
        }
    }

    @Throws(KommandException::class)
    actual fun waitWithOutput(): Output {
        stdin.get()?.close()
        val stdoutContent = runCatching { bufferedStdout()?.readAll() }
            .getOrElse { throw KommandException("Child has been consumed", ErrorType.None) }
        val stderrContent = runCatching { bufferedStderr()?.readAll() }
            .getOrElse { throw KommandException("Child has been consumed", ErrorType.None) }
        val status = process.waitFor()
        stdout.get()?.close()
        stderr.get()?.close()
        return Output(status, stdoutContent, stderrContent)
    }
}
