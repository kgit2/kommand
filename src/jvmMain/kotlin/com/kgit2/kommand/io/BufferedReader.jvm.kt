package com.kgit2.kommand.io

import com.kgit2.kommand.exception.KommandException

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

    @Throws(KommandException::class)
    actual fun lines(): Sequence<String> {
        return reader.lineSequence()
    }

    fun close() {
        reader.close()
    }
}
