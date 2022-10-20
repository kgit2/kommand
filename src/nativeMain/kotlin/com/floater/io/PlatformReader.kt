package com.floater.io

import io.ktor.utils.io.bits.*
import kotlinx.cinterop.*
import platform.posix.*

actual class PlatformReader {
    var file: CPointer<FILE>? = null

    constructor(
        file: CPointer<FILE>
    ) {
        this.file = file
    }

    actual fun closeSource() {
        fclose(file)
    }

    actual fun fill(destination: Memory, offset: Int, length: Int): Int {
        return memScoped {
            val buffer = allocArray<ByteVar>(length)
            val read = fread(buffer, 1, length.toULong(), file)
            buffer.copyTo(destination, offset.toLong(), read.toLong(), 0)
            read.toInt()
        }
    }
}
