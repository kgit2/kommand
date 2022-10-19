package com.floater.io

import io.ktor.utils.io.bits.*
import java.io.OutputStream

actual class PlatformWriter {
    var outputStream: OutputStream? = null

    constructor(
        outputStream: OutputStream
    ) {
        this.outputStream = outputStream
    }

    actual fun flush(source: Memory, offset: Int, length: Int) {
        val buffer = ByteArray(length)
        source.copyTo(buffer, offset, length, 0)
        outputStream?.write(buffer)
    }

    actual fun close() {
        this.outputStream?.close()
    }
}
