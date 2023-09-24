package com.kgit2.io

import io.ktor.utils.io.bits.*
import java.io.BufferedReader
import java.io.InputStream
import java.util.LinkedList
import java.util.Queue
import kotlin.math.min

actual class PlatformReader(inputStream: InputStream) {
    private var inputStream: BufferedReader? = null

    init {
        this.inputStream = inputStream.bufferedReader()
    }

    actual fun closeSource() {
        inputStream?.close()
    }

    private val buffer: Queue<Byte> = LinkedList()

    actual fun fill(destination: Memory, offset: Int, length: Int): Int {
        var readed = 0
        val migrateBuffer = {
            for (i in 0 until min(length, buffer.size)) {
                destination.storeAt(offset + i, buffer.poll())
                readed += 1
            }
        }
        when {
            length < buffer.size -> {
                println("length < buffer.size")
                migrateBuffer()
            }
            else -> {
                inputStream?.readLine()?.apply {
                    buffer.addAll("$this\n".toByteArray(Charsets.UTF_8).toList())
                }
                migrateBuffer()
            }
        }
        return readed
    }
}
