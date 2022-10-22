package com.floater.io

import io.ktor.utils.io.bits.*
import java.io.OutputStream

actual class PlatformWriter(outputStream: OutputStream) {
    var outputStream: OutputStream? = outputStream

    actual fun flush(source: Memory, offset: Int, length: Int) {
        val buffer = ByteArray(length)
        source.copyTo(buffer, offset, length, 0)
        outputStream?.write(buffer)
        outputStream?.flush()
    }

    actual fun close() {
        this.outputStream?.close()
    }
}
