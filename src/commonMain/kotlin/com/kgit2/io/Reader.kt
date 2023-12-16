package com.kgit2.io

import io.ktor.utils.io.bits.*
import io.ktor.utils.io.core.*

expect class PlatformReader {
    fun closeSource()

    fun fill(destination: Memory, offset: Int, length: Int): Int
}

open class Reader (
    private val platformReader: PlatformReader
) : Input(
    // pool = DefaultBufferPool(Buffer.ReservedSize + 1, 1)
) {

    override fun closeSource() {
        platformReader.closeSource()
    }

    override fun fill(destination: Memory, offset: Int, length: Int): Int {
        return platformReader.fill(destination, offset, length)
    }

    fun readLine(): String? {
        return readUTF8Line()
    }

    fun lines(): Sequence<String> {
        return sequence {
            while (endOfInput.not()) {
                readUTF8Line()?.let { yield(it) }
            }
        }
    }
}
