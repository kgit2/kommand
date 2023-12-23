package com.kgit2.process

import com.kgit2.exception.KommandException
import com.kgit2.io.BufferedReader
import com.kgit2.io.BufferedWriter
import com.kgit2.io.Output

actual class Child(
    private val process: Process,
) {
    var stdin: BufferedWriter? = null
    var stdout: BufferedReader? = null
    var stderr: BufferedReader? = null

    actual fun id(): UInt {
        TODO("Not yet implemented")
    }

    actual fun bufferedStdin(): BufferedWriter? {
        TODO("Not yet implemented")
    }

    actual fun bufferedStdout(): BufferedReader? {
        TODO("Not yet implemented")
    }

    actual fun bufferedStderr(): BufferedReader? {
        TODO("Not yet implemented")
    }

    @Throws(KommandException::class)
    actual fun kill() {}

    @Throws(KommandException::class)
    actual fun wait(): Int {
        return 0
    }

    @Throws(KommandException::class)
    actual fun waitWithOutput(): Output {
        TODO("Not yet implemented")
    }
}
