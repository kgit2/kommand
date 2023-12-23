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
import kotlinx.cinterop.COpaquePointer
import kotlin.native.ref.createCleaner

actual class Child(
    private var inner: COpaquePointer?
) {
    var stdin: BufferedWriter? = null
    var stdout: BufferedReader? = null
    var stderr: BufferedReader? = null

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
        if (stdin == null) {
            updateStdin()
        }
        return stdin
    }

    actual fun bufferedStdout(): BufferedReader? {
        if (stdout == null) {
            updateStdout()
        }
        return stdout
    }

    actual fun bufferedStderr(): BufferedReader? {
        if (stderr == null) {
            updateStderr()
        }
        return stderr
    }

    @Throws(KommandException::class)
    actual fun kill() = run {
        killChild(inner)
    }

    @Throws(KommandException::class)
    actual fun wait(): Int = run {
        stdin?.close()
        waitChild(inner)
    }

    @Throws(KommandException::class)
    actual fun waitWithOutput(): Output = run {
        stdin?.close()
        val inner = this.inner
        this.inner = null
        isClosed.getAndSet(true)
        waitWithOutputChild(inner)
    }

    private fun updateStdin() {
        stdin = bufferedStdinChild(inner)
    }

    private fun updateStdout() {
        stdout = bufferedStdoutChild(inner)
    }

    private fun updateStderr() {
        stderr = bufferedStderrChild(inner)
    }
}
