package com.kgit2.io

import io.ktor.utils.io.bits.*
import stream.internal

actual class PlatformWriter(writeable: internal.Writable) {
    var writeable: internal.Writable? = writeable

    @OptIn(ExperimentalUnsignedTypes::class)
    actual fun flush(source: Memory, offset: Int, length: Int) {
        val buffer = ByteArray(length)
        source.copyTo(buffer, offset, length, 0)
        writeable?.write(buffer.toUByteArray())
    }

    actual fun close() {
        writeable?.destroy()
    }
}
