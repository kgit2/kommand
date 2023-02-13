package com.kgit2.io

import io.ktor.utils.io.bits.*
import java.io.BufferedReader
import java.io.InputStream

actual class PlatformReader(inputStream: InputStream) {
    private var inputStream: BufferedReader? = null

    init {
        this.inputStream = inputStream.bufferedReader()
    }

    actual fun closeSource() {
        inputStream?.close()
    }

    actual fun fill(destination: Memory, offset: Int, length: Int): Int {
        val buffer = CharArray(length)
        var readed = 0
        while (length > readed && inputStream?.ready() == true) {
            val readNow = inputStream?.read(buffer, readed, length - readed) ?: 0
            if (readNow == -1) {
                break
            }
            readed += readNow
        }
        for (i in 0 until readed) {
            destination.storeAt(offset + i, buffer[i].code.toByte())
        }
        return readed
    }

    actual fun fillLine(destination: Memory, offset: Int, length: Int): Int {
        return try {
            val line = inputStream?.readLine() ?: return 0
            var readed = 0
            line.forEachIndexed { index, c ->
                destination.storeAt(offset + index, c.code.toByte())
                readed += 1
            }
            destination.storeAt(offset + readed, 10)
            readed += 1
            readed
        } catch (e: Exception) {
            0
        }
    }
}
