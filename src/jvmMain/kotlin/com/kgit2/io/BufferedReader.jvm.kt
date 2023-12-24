package com.kgit2.io

import com.kgit2.exception.KommandException

actual class BufferedReader(
    private val reader: java.io.BufferedReader,
) {
    @Throws(KommandException::class)
    actual fun readLine(): String? {
        return reader.readLine()
    }

    @Throws(KommandException::class)
    actual fun readAll(): String? {
        return reader.readText()
    }

    fun close() {
        reader.close()
    }
}
