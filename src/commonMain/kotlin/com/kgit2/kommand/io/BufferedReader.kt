package com.kgit2.kommand.io

import com.kgit2.kommand.exception.KommandException

expect class BufferedReader {
    @Throws(KommandException::class)
    fun readLine(): String?

    @Throws(KommandException::class)
    fun readAll(): String?

    @Throws(KommandException::class)
    fun lines(): Sequence<String>
}
