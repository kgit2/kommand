package com.kgit2.wrapper

import com.kgit2.exception.KommandException
import com.kgit2.io.BufferedReader
import com.kgit2.io.BufferedWriter
import com.kgit2.io.Output
import com.kgit2.io.ReaderType
import kommand_core.drop_child
import kommand_core.id_child
import kommand_core.kill_child
import kommand_core.stderr_child
import kommand_core.stdin_child
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

actual fun stdinChild(child: COpaquePointer?): BufferedWriter? {
    return BufferedWriter(stdin_child(child))
}

actual fun stdoutChild(child: COpaquePointer?): BufferedReader? {
    return BufferedReader(stdin_child(child), ReaderType.STDOUT)
}

actual fun stderrChild(child: COpaquePointer?): BufferedReader? {
    return BufferedReader(stderr_child(child), ReaderType.STDERR)
}
