package com.kgit2.io

import io.ktor.utils.io.bits.*
import stream.internal

actual class PlatformReader(readable: internal.Readable) {
    var readable: internal.Readable? = readable

    init {
        readable.setEncoding("utf8")
    }

    actual fun closeSource() {
        readable?.destroy()
    }

    actual fun fill(destination: Memory, offset: Int, length: Int): Int {
        val buffer = ByteArray(length)
        var readed = 0
        while (readable?.readable == true) {
            readable?.read()
        }
        return readed
    }

    actual fun fillLine(destination: Memory, offset: Int, length: Int): Int {
        TODO("Not yet implemented")
    }

}
