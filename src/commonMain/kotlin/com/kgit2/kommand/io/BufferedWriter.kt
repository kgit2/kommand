package com.kgit2.kommand.io

import com.kgit2.kommand.exception.KommandException

expect class BufferedWriter {
    @Throws(KommandException::class)
    fun writeLine(line: String)

    @Throws(KommandException::class)
    fun flush()

    @Throws(KommandException::class)
    fun close()
}
