package com.kgit2.kommand.io

import com.kgit2.kommand.exception.KommandException
import com.kgit2.kommand.wrapper.dropStderr
import com.kgit2.kommand.wrapper.dropStdout
import com.kgit2.kommand.wrapper.readAllStderr
import com.kgit2.kommand.wrapper.readAllStdout
import com.kgit2.kommand.wrapper.readLineStderr
import com.kgit2.kommand.wrapper.readLineStdout
import kotlinx.cinterop.COpaquePointer
import kotlin.native.ref.createCleaner

actual class BufferedReader(
    private val inner: COpaquePointer?,
    private val type: ReaderType,
) {

    val cleaner = createCleaner(inner) { reader ->
        when (type) {
            ReaderType.STDOUT -> {
                dropStdout(reader)
            }
            ReaderType.STDERR -> {
                dropStderr(reader)
            }
        }
    }

    @Throws(KommandException::class)
    actual fun readLine(): String? = run {
        when (type) {
            ReaderType.STDOUT -> {
                readLineStdout(inner)
            }
            ReaderType.STDERR -> {
                readLineStderr(inner)
            }
        }
    }

    @Throws(KommandException::class)
    actual fun readAll(): String? = run {
        when (type) {
            ReaderType.STDOUT -> {
                readAllStdout(inner)
            }
            ReaderType.STDERR -> {
                readAllStderr(inner)
            }
        }
    }
}

enum class ReaderType {
    STDOUT,
    STDERR,
    ;
}
