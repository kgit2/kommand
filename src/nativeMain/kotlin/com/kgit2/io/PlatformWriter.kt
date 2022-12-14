package com.kgit2.io

import io.ktor.utils.io.bits.*
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import platform.posix.FILE
import platform.posix.fclose
import platform.posix.fflush
import platform.posix.fwrite

actual class PlatformWriter(file: CPointer<FILE>) {
    var file: CPointer<FILE>? = file

    actual fun flush(source: Memory, offset: Int, length: Int) {
        val buffer = ByteArray(length)
        source.copyTo(buffer, offset, length, 0)
        buffer.usePinned {
            fwrite(it.addressOf(0), 1, length.convert(), file)
        }
        fflush(file)
    }

    actual fun close() {
        fclose(file)
    }
}
