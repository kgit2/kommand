package com.kgit2.io

import io.ktor.utils.io.bits.*
import io.ktor.utils.io.core.*
import io.ktor.utils.io.core.internal.*

expect class PlatformReader {
    fun closeSource()

    fun fill(destination: Memory, offset: Int, length: Int): Int

    fun fillLine(destination: Memory, offset: Int, length: Int): Int
}

open class Reader (
    private val platformReader: PlatformReader
) : Input(
    // pool = DefaultBufferPool(Buffer.ReservedSize + 1, 1)
) {

    var currentReadLine = false

    override fun closeSource() {
        platformReader.closeSource()
    }

    override fun fill(destination: Memory, offset: Int, length: Int): Int {
        return platformReader.fill(destination, offset, length)
    }

    fun fillLine(destination: Memory, offset: Int, length: Int): Int {
        return platformReader.fillLine(destination, offset, length)
    }

    override fun fill(): ChunkBuffer? {
        val buffer = pool.borrow()
        try {
            buffer.reserveEndGap(Buffer.ReservedSize)
            val copied = if (currentReadLine)
                fillLine(buffer.memory, buffer.writePosition, buffer.writeRemaining)
                else fill(buffer.memory, buffer.writePosition, buffer.writeRemaining)

            if (copied == 0) {
                markNoMoreChunksAvailable()

                if (!buffer.canRead()) {
                    buffer.release(pool)
                    return null
                }
            }

            buffer.commitWritten(copied)

            return buffer
        } catch (t: Throwable) {
            buffer.release(pool)
            throw t
        }
    }

    fun readLine(): String? {
        this.currentReadLine = true
        val result = readUTF8Line()
        this.currentReadLine = false
        return result
    }

    fun lines(): Sequence<String> {
        return sequence {
            this@Reader.currentReadLine = true
            while (endOfInput.not()) {
                readLine()?.let { yield(it) }
            }
        }
    }
}
