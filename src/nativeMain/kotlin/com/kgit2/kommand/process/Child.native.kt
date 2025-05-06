package com.kgit2.kommand.process

import com.kgit2.kommand.exception.KommandException
import com.kgit2.kommand.from
import com.kgit2.kommand.fromOptional
import com.kgit2.kommand.io.BufferedReader
import com.kgit2.kommand.io.BufferedWriter
import com.kgit2.kommand.io.Output
import com.kgit2.kommand.io.ReaderType
import com.kgit2.kommand.unwrap
import kommand_core.*
import kommand_core.try_wait_child
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.cinterop.COpaquePointer
import kotlin.concurrent.AtomicReference
import kotlin.native.ref.createCleaner

actual class Child(
    private var inner: COpaquePointer?
) : SynchronizedObject() {
    private var stdin: AtomicReference<BufferedWriter?> = AtomicReference(null)
    private var stdout: AtomicReference<BufferedReader?> = AtomicReference(null)
    private var stderr: AtomicReference<BufferedReader?> = AtomicReference(null)

    private val isClosed = atomic(false)

    private val cleaner = createCleaner(isClosed to inner) { (freed, child) ->
        if (freed.compareAndSet(expect = false, update = true)) {
            drop_child(child)
        }
    }

    companion object;

    actual fun id(): UInt {
        return id_child(inner)
    }

    actual fun bufferedStdin(): BufferedWriter? {
        stdin.compareAndSet(null, BufferedWriter(buffered_stdin_child(inner)))
        return stdin.value
    }

    actual fun bufferedStdout(): BufferedReader? {
        stdout.compareAndSet(null, BufferedReader(buffered_stdout_child(inner), ReaderType.STDOUT))
        return stdout.value
    }

    actual fun bufferedStderr(): BufferedReader? {
        stderr.compareAndSet(null, BufferedReader(buffered_stderr_child(inner), ReaderType.STDERR))
        return stderr.value
    }

    @Throws(KommandException::class)
    actual fun kill() = run {
        kill_child(inner).unwrap()
    }

    @Throws(KommandException::class)
    actual fun wait(): Int = run {
        stdin.getAndSet(null)?.close()
        Int.from(wait_child(inner))
    }

    @Throws(KommandException::class)
    actual fun tryWait(): Int? {
        return when (Int.fromOptional(try_wait_child(inner))) {
            null -> null
            else -> wait()
        }
    }

    @Throws(KommandException::class)
    actual fun waitWithOutput(): Output = run {
        stdin.getAndSet(null)?.close()
        val inner = this.inner
        this.inner = null
        isClosed.getAndSet(true)
        Output.from(wait_with_output_child(inner))
    }
}
