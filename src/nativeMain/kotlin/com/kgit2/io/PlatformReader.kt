package com.kgit2.io

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
            val read = fread(buffer, 1, length.convert(), file)
            destination.storeByteArray(0, buffer.readBytes(read.convert()))
            read.convert()
        }
    }

    actual fun fillLine(destination: Memory, offset: Int, length: Int): Int {
        return memScoped {
            val buffer = allocArray<ByteVar>(destination.size)
            val line = fgets(buffer, length, file)
            val lineString = line?.toKString()
            val len = lineString?.length ?: 0
            destination.storeByteArray(offset.convert(), buffer.readBytes(len.convert()))
            len.convert()
        }
    }
}
