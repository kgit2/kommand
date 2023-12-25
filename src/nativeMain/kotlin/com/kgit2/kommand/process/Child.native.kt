package com.kgit2.kommand.process

import com.kgit2.kommand.exception.KommandException
import com.kgit2.kommand.io.BufferedReader
import com.kgit2.kommand.io.BufferedWriter
import com.kgit2.kommand.io.Output
import com.kgit2.kommand.wrapper.bufferedStderrChild
import com.kgit2.kommand.wrapper.bufferedStdinChild
import com.kgit2.kommand.wrapper.bufferedStdoutChild
import com.kgit2.kommand.wrapper.dropChild
import com.kgit2.kommand.wrapper.killChild
import com.kgit2.kommand.wrapper.waitChild
import com.kgit2.kommand.wrapper.waitWithOutputChild
import kommand_core.id_child
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.cinterop.COpaquePointer
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
        return id_child(inner)
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
