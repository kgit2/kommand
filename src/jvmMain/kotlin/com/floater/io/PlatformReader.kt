package com.floater.io

import io.ktor.utils.io.bits.*
import java.io.InputStream

actual class PlatformReader {
    var inputStream: InputStream? = null

    constructor(
        inputStream: InputStream
    ) {
        this.inputStream = inputStream
    }

    actual fun closeSource() {
        inputStream?.close()
    }

    actual fun fill(destination: Memory, offset: Int, length: Int): Int {
        val buffer = ByteArray(length)
        val read = inputStream?.read(buffer) ?: 0
        buffer.withIndex().forEach {
            destination.storeAt(offset + it.index, it.value)
        }
        return read
    }
}
