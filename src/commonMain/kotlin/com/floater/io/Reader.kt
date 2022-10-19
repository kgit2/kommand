package com.floater.io

import io.ktor.utils.io.bits.*
import io.ktor.utils.io.core.*

expect class PlatformReader {
    fun closeSource()

    fun fill(destination: Memory, offset: Int, length: Int): Int
}

open class Reader (
    private val platformReader: PlatformReader
) : Input() {

    override fun closeSource() {
        platformReader.closeSource()
    }

    override fun fill(destination: Memory, offset: Int, length: Int): Int {
        return platformReader.fill(destination, offset, length)
    }
}
