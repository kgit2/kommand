package com.kgit2.io

import io.ktor.utils.io.bits.*

actual class PlatformWriter {
    actual fun flush(source: Memory, offset: Int, length: Int) {
    }

    actual fun close() {
    }
}
