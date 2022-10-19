package com.floater.io

import io.ktor.utils.io.bits.*
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import platform.posix.FILE
import platform.posix.fclose
import platform.posix.fread

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
        memScoped {
            val buffer = allocArray<ByteVar>(length)
            val read = fread(buffer, 1, length.toULong(), file)
            buffer.copyTo(destination, offset.toLong(), read.toLong(), 0)
            return read.toInt()
        }
    }
}
