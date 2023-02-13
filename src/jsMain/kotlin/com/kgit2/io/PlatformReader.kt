package com.kgit2.io

import io.ktor.utils.io.bits.*

actual class PlatformReader {
    actual fun closeSource() {
    }

    actual fun fill(destination: Memory, offset: Int, length: Int): Int {
        TODO("Not yet implemented")
    }

    actual fun fillLine(destination: Memory, offset: Int, length: Int): Int {
        TODO("Not yet implemented")
    }

}
