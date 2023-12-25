package com.kgit2.kommand.io

import com.kgit2.kommand.exception.KommandException
import com.kgit2.kommand.from
import kommand_core.drop_stderr
import kommand_core.drop_stdout
import kommand_core.read_all_stderr
import kommand_core.read_all_stdout
import kommand_core.read_line_stderr
import kommand_core.read_line_stdout
import kotlinx.cinterop.COpaquePointer
import kotlin.native.ref.createCleaner

actual class BufferedReader(
    private val inner: COpaquePointer?,
    private val type: ReaderType,
) {

    val cleaner = createCleaner(inner) { reader ->
        when (type) {
            ReaderType.STDOUT -> {
                drop_stdout(reader)
            }
            ReaderType.STDERR -> {
                drop_stderr(reader)
            }
        }
    }

    @Throws(KommandException::class)
    actual fun readLine(): String? = run {
        when (type) {
            ReaderType.STDOUT -> {
                String.from(read_line_stdout(inner))
            }
            ReaderType.STDERR -> {
                String.from(read_line_stderr(inner))
            }
        }
    }

    @Throws(KommandException::class)
    actual fun readAll(): String? = run {
        when (type) {
            ReaderType.STDOUT -> {
                String.from(read_all_stdout(inner))
            }
            ReaderType.STDERR -> {
                String.from(read_all_stderr(inner))
            }
        }
    }
}

enum class ReaderType {
    STDOUT,
    STDERR,
    ;
}
