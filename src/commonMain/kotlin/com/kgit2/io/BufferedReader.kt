package com.kgit2.io

import com.kgit2.exception.KommandException

expect class BufferedReader {
    @Throws(KommandException::class)
    fun readLine(): String?

    @Throws(KommandException::class)
    fun readAll(): String?
}
