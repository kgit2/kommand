package com.kgit2.kommand.io

import com.kgit2.kommand.exception.KommandException
import com.kgit2.kommand.unwrap
import kommand_core.drop_stdin
import kommand_core.flush_stdin
import kommand_core.write_line_stdin
import kotlinx.atomicfu.atomic
import kotlinx.cinterop.COpaquePointer
import kotlin.native.ref.createCleaner

actual class BufferedWriter(
    private var inner: COpaquePointer?
) {
    private val isClosed = atomic(false)

    private val cleaner = createCleaner(isClosed to inner) { (freed, writter) ->
        if (freed.compareAndSet(expect = false, update = true)) {
            drop_stdin(writter)
        }
    }

    @Throws(KommandException::class)
    actual fun writeLine(line: String) = run {
        write_line_stdin(inner, line).unwrap()
    }

    @Throws(KommandException::class)
    actual fun flush() = run {
        flush_stdin(inner).unwrap()
    }

    @Throws(KommandException::class)
    actual fun close() {
        drop_stdin(inner)
        isClosed.getAndSet(true)
    }
}
