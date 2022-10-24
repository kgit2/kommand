package com.kgit2.io

import io.ktor.utils.io.bits.*
import io.ktor.utils.io.core.*

expect class PlatformWriter {
    fun flush(source: Memory, offset: Int, length: Int)
    fun close()
}

open class Writer (
    private val platformWriter: PlatformWriter
) : Output() {
    override fun closeDestination() {
        platformWriter.close()
    }

    override fun flush(source: Memory, offset: Int, length: Int) {
        platformWriter.flush(source, offset, length)
    }
}
