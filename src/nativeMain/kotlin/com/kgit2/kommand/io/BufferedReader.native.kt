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
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import kotlin.native.ref.createCleaner

actual class BufferedReader(
    private val inner: COpaquePointer?,
    private val type: ReaderType,
) {

    val cleaner = createCleaner(inner to type) { (reader, type) ->
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
        memScoped {
            val size = alloc(0uL)
            val result = when (type) {
                ReaderType.STDOUT -> {
                    read_line_stdout(inner, size.ptr)
                }
                ReaderType.STDERR -> {
                    read_line_stderr(inner, size.ptr)
                }
            }
            if (size.value == 0uL) {
                null
            } else {
                String.from(result)
            }
        }
    }

    @Throws(KommandException::class)
    actual fun readAll(): String? = run {
        memScoped {
            val size = alloc(0uL)
            val result = when (type) {
                ReaderType.STDOUT -> {
                    read_all_stdout(inner, size.ptr)
                }
                ReaderType.STDERR -> {
                    read_all_stderr(inner, size.ptr)
                }
            }
            if (size.value == 0uL) {
                null
            } else {
                String.from(result)
            }
        }
    }

    @Throws(KommandException::class)
    actual fun lines(): Sequence<String> = sequence {
        do {
            val line = readLine()
            if (line != null) {
                yield(line)
            }
        } while (line != null)
    }
}

enum class ReaderType {
    STDOUT,
    STDERR,
    ;
}
