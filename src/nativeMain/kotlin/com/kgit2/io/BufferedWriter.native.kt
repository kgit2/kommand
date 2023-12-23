package com.kgit2.io

import com.kgit2.exception.KommandException
import com.kgit2.wrapper.dropStdin
import com.kgit2.wrapper.flushStdin
import com.kgit2.wrapper.writeLineStdin
import kotlinx.cinterop.COpaquePointer
import kotlin.native.ref.createCleaner

actual class BufferedWriter(
    private val inner: COpaquePointer?
) {
    val cleaner = createCleaner(inner) { writer ->
        dropStdin(writer)
    }

    @Throws(KommandException::class)
    actual fun writeLine(line: String) = run {
        writeLineStdin(inner, line)
    }

    @Throws(KommandException::class)
    actual fun flush() = run {
        flushStdin(inner)
    }
}
