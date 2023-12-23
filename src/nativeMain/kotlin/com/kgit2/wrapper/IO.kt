package com.kgit2.wrapper

import com.kgit2.exception.KommandException
import kotlinx.cinterop.COpaquePointer

@Throws(KommandException::class)
expect fun readLineStdout(stdout: COpaquePointer?): String?

@Throws(KommandException::class)
expect fun readAllStdout(stdout: COpaquePointer?): String?

@Throws(KommandException::class)
expect fun readLineStderr(stderr: COpaquePointer?): String?

@Throws(KommandException::class)
expect fun readAllStderr(stderr: COpaquePointer?): String?

@Throws(KommandException::class)
expect fun writeLineStdin(stdin: COpaquePointer?, line: String)

@Throws(KommandException::class)
expect fun flushStdin(stdin: COpaquePointer?)

expect fun dropStdin(stdin: COpaquePointer?)

expect fun dropStdout(stdout: COpaquePointer?)

expect fun dropStderr(stderr: COpaquePointer?)
