package com.kgit2.wrapper

import com.kgit2.exception.KommandException
import kommand_core.drop_stderr
import kommand_core.drop_stdin
import kommand_core.drop_stdout
import kommand_core.flush_stdin
import kommand_core.read_all_stderr
import kommand_core.read_all_stdout
import kommand_core.read_line_stderr
import kommand_core.read_line_stdout
import kommand_core.write_line_stdin
import kotlinx.cinterop.COpaquePointer

@Throws(KommandException::class)
actual fun readLineStdout(stdout: COpaquePointer?): String? = run {
    String.from(read_line_stdout(stdout))
}

@Throws(KommandException::class)
actual fun readAllStdout(stdout: COpaquePointer?): String? = run {
    String.from(read_all_stdout(stdout))
}

@Throws(KommandException::class)
actual fun readLineStderr(stderr: COpaquePointer?): String? = run {
    String.from(read_line_stderr(stderr))
}

@Throws(KommandException::class)
actual fun readAllStderr(stderr: COpaquePointer?): String? = run {
    String.from(read_all_stderr(stderr))
}

@Throws(KommandException::class)
actual fun writeLineStdin(stdin: COpaquePointer?, line: String) = run {
    write_line_stdin(stdin, line).unwrap()
}

@Throws(KommandException::class)
actual fun flushStdin(stdin: COpaquePointer?) = run {
    flush_stdin(stdin).unwrap()
}

actual fun dropStdin(stdin: COpaquePointer?) {
    drop_stdin(stdin)
}

actual fun dropStdout(stdout: COpaquePointer?) {
    drop_stdout(stdout)
}

actual fun dropStderr(stderr: COpaquePointer?) {
    drop_stderr(stderr)
}
