package com.floater.io

import io.ktor.utils.io.bits.*
import io.ktor.utils.io.core.*

// expect class InnerWriter {
//     fun write(buffer: ByteArray, offset: Int, length: Int): Int
//     fun flush()
//     fun close()
// }

open class Writer : Output() {
    override fun closeDestination() {
        TODO("Not yet implemented")
    }

    override fun flush(source: Memory, offset: Int, length: Int) {
        TODO("Not yet implemented")
    }
}
