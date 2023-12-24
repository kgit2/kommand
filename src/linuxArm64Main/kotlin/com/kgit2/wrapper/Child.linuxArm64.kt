package com.kgit2.wrapper

import com.kgit2.exception.KommandException
import com.kgit2.io.BufferedReader
import com.kgit2.io.BufferedWriter
import com.kgit2.io.Output
import com.kgit2.io.ReaderType
import kommand_core.buffered_stderr_child
import kommand_core.buffered_stdin_child
import kommand_core.buffered_stdout_child
import kommand_core.drop_child
import kommand_core.id_child
import kommand_core.kill_child
import kommand_core.wait_child
import kommand_core.wait_with_output_child
import kotlinx.cinterop.COpaquePointer

actual fun idChild(child: COpaquePointer?): UInt {
    return id_child(child)
}

actual fun dropChild(child: COpaquePointer?) {
    drop_child(child)
}

@Throws(KommandException::class)
actual fun killChild(child: COpaquePointer?) = run {
    kill_child(child).unwrap()
}

@Throws(KommandException::class)
actual fun waitChild(child: COpaquePointer?): Int = run {
    Int.from(wait_child(child))
}

@Throws(KommandException::class)
actual fun waitWithOutputChild(child: COpaquePointer?): Output = run {
    Output.from(wait_with_output_child(child))
}

actual fun bufferedStdinChild(child: COpaquePointer?): BufferedWriter? {
    return BufferedWriter(buffered_stdin_child(child))
}

actual fun bufferedStdoutChild(child: COpaquePointer?): BufferedReader? {
    return BufferedReader(buffered_stdout_child(child), ReaderType.STDOUT)
}

actual fun bufferedStderrChild(child: COpaquePointer?): BufferedReader? {
    return BufferedReader(buffered_stderr_child(child), ReaderType.STDERR)
}
