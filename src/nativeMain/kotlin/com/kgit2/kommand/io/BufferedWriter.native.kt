package com.kgit2.kommand.io

import com.kgit2.kommand.exception.KommandException
import com.kgit2.kommand.wrapper.dropStdin
import com.kgit2.kommand.wrapper.flushStdin
import com.kgit2.kommand.wrapper.writeLineStdin
import kotlinx.atomicfu.atomic
import kotlinx.cinterop.COpaquePointer
import kotlin.native.ref.createCleaner

actual class BufferedWriter(
    private var inner: COpaquePointer?
) {
    private val isClosed = atomic(false)

    private val cleaner = createCleaner(isClosed to inner) { (freed, writer) ->
        if (freed.compareAndSet(expect = false, update = true)) {
            dropStdin(writer)
        }
    }

    @Throws(KommandException::class)
    actual fun writeLine(line: String) = run {
        writeLineStdin(inner, line)
    }

    @Throws(KommandException::class)
    actual fun flush() = run {
        flushStdin(inner)
    }

    @Throws(KommandException::class)
    actual fun close() {
        dropStdin(inner)
        isClosed.getAndSet(true)
    }
}
