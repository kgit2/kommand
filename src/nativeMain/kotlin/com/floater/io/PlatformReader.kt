package com.floater.io

import io.ktor.utils.io.bits.*
import kotlinx.cinterop.*
import platform.posix.*

actual class PlatformReader(file: CPointer<FILE>) {
    var file: CPointer<FILE>? = file

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

    actual fun fillLine(destination: Memory, offset: Int, length: Int): Int {
        return memScoped {
            val buffer = allocArray<ByteVar>(destination.size)
            val line = fgets(buffer, length, file)
            val lineString = line?.toKString()
            val len = lineString?.length ?: 0
            buffer.copyTo(destination, offset.convert(), len.convert(), 0)
            len.convert()
        }
    }
}
