package com.kgit2.process

import com.kgit2.exception.KommandException
import com.kgit2.io.BufferedReader
import com.kgit2.io.BufferedWriter
import com.kgit2.io.Output

expect class Child {
    fun id(): UInt

    fun bufferedStdin(): BufferedWriter?

    fun bufferedStdout(): BufferedReader?

    fun bufferedStderr(): BufferedReader?

    @Throws(KommandException::class)
    fun kill()

    @Throws(KommandException::class)
    fun wait(): Int

    @Throws(KommandException::class)
    fun waitWithOutput(): Output
}
