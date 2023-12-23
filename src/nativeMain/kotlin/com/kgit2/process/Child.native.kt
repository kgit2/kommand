package com.kgit2.process

import com.kgit2.exception.KommandException
import com.kgit2.io.BufferedReader
import com.kgit2.io.BufferedWriter
import com.kgit2.io.Output
import com.kgit2.wrapper.dropChild
import com.kgit2.wrapper.idChild
import com.kgit2.wrapper.killChild
import com.kgit2.wrapper.stderrChild
import com.kgit2.wrapper.stdinChild
import com.kgit2.wrapper.stdoutChild
import com.kgit2.wrapper.waitChild
import com.kgit2.wrapper.waitWithOutputChild
import kotlinx.cinterop.COpaquePointer
import kotlin.native.ref.createCleaner

actual class Child(
    private val inner: COpaquePointer?
) {
    actual var stdin: BufferedWriter? = null
    actual var stdout: BufferedReader? = null
    actual var stderr: BufferedReader? = null

    val cleaner = createCleaner(inner) { child ->
        dropChild(child)
    }

    companion object;

    actual fun id(): UInt {
        return idChild(inner)
    }

    @Throws(KommandException::class)
    actual fun kill() {
        return run { killChild(inner) }
    }

    @Throws(KommandException::class)
    actual fun wait(): Int {
        return run { waitChild(inner) }
    }

    @Throws(KommandException::class)
    actual fun waitWithOutput(): Output {
        return run { waitWithOutputChild(inner) }
    }

    internal fun updateIO() {
        updateStdin()
        updateStdout()
        updateStderr()
    }

    @Suppress("MemberVisibilityCanBePrivate")
    internal fun updateStdin() {
        stdin = stdinChild(inner)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    internal fun updateStdout() {
        stdout = stdoutChild(inner)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    internal fun updateStderr() {
        stderr = stderrChild(inner)
    }
}
