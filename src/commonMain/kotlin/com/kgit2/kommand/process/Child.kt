package com.kgit2.kommand.process

import com.kgit2.kommand.exception.KommandException
import com.kgit2.kommand.io.BufferedReader
import com.kgit2.kommand.io.BufferedWriter
import com.kgit2.kommand.io.Output

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
    fun tryWait(): Int?

    @Throws(KommandException::class)
    fun waitWithOutput(): Output
}
