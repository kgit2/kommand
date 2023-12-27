package com.kgit2.kommand.io

import com.kgit2.kommand.exception.KommandException

actual class BufferedWriter(
    private val writer: java.io.BufferedWriter,
) {
    @Throws(KommandException::class)
    actual fun writeLine(line: String) {
        writer.write(line)
        writer.newLine()
    }

    @Throws(KommandException::class)
    actual fun flush() {
        writer.flush()
    }

    @Throws(KommandException::class)
    actual fun close() {
        writer.close()
    }
}
