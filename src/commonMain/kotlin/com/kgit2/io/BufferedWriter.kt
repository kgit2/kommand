package com.kgit2.io

import com.kgit2.exception.KommandException

expect class BufferedWriter {
    @Throws(KommandException::class)
    fun writeLine(line: String)

    @Throws(KommandException::class)
    fun flush()

    @Throws(KommandException::class)
    fun close()
}
