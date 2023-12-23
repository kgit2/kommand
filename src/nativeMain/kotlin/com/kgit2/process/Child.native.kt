package com.kgit2.process

import com.kgit2.exception.KommandException
import com.kgit2.io.BufferedReader
import com.kgit2.io.BufferedWriter
import com.kgit2.io.Output
import com.kgit2.wrapper.dropChild
import com.kgit2.wrapper.idChild
import com.kgit2.wrapper.killChild
import com.kgit2.wrapper.bufferedStderrChild
import com.kgit2.wrapper.bufferedStdinChild
import com.kgit2.wrapper.bufferedStdoutChild
import com.kgit2.wrapper.waitChild
import com.kgit2.wrapper.waitWithOutputChild
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.cinterop.COpaquePointer
import kotlinx.coroutines.internal.synchronized
import kotlin.concurrent.AtomicReference
import kotlin.native.ref.createCleaner

actual class Child(
    private var inner: COpaquePointer?
): SynchronizedObject() {
    private var stdin: AtomicReference<BufferedWriter?> = AtomicReference(null)
    private var stdout: AtomicReference<BufferedReader?> = AtomicReference(null)
    private var stderr: AtomicReference<BufferedReader?> = AtomicReference(null)

    private val isClosed = atomic(false)

    private val cleaner = createCleaner(isClosed to inner) { (freed, child) ->
        if (freed.compareAndSet(expect = false, update = true)) {
            dropChild(child)
        }
    }

    companion object;

    actual fun id(): UInt {
        return idChild(inner)
    }

    actual fun bufferedStdin(): BufferedWriter? {
        stdin.compareAndSet(null, bufferedStdinChild(inner))
        return stdin.value
    }

    actual fun bufferedStdout(): BufferedReader? {
        stdout.compareAndSet(null, bufferedStdoutChild(inner))
        return stdout.value
    }

    actual fun bufferedStderr(): BufferedReader? {
        stderr.compareAndSet(null, bufferedStderrChild(inner))
        return stderr.value
    }

    @Throws(KommandException::class)
    actual fun kill() = run {
        killChild(inner)
    }

    @Throws(KommandException::class)
    actual fun wait(): Int = run {
        stdin.getAndSet(null)?.close()
        waitChild(inner)
    }

    @Throws(KommandException::class)
    actual fun waitWithOutput(): Output = run {
        stdin.getAndSet(null)?.close()
        val inner = this.inner
        this.inner = null
        isClosed.getAndSet(true)
        waitWithOutputChild(inner)
    }
}
