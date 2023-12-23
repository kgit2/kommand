package com.kgit2.wrapper

import com.kgit2.exception.KommandException
import com.kgit2.io.BufferedReader
import com.kgit2.io.BufferedWriter
import com.kgit2.io.Output
import kotlinx.cinterop.COpaquePointer

expect fun idChild(child: COpaquePointer?): UInt

expect fun dropChild(child: COpaquePointer?)

@Throws(KommandException::class)
expect fun killChild(child: COpaquePointer?)

@Throws(KommandException::class)
expect fun waitChild(child: COpaquePointer?): Int

@Throws(KommandException::class)
expect fun waitWithOutputChild(child: COpaquePointer?): Output

expect fun stdinChild(child: COpaquePointer?): BufferedWriter?

expect fun stdoutChild(child: COpaquePointer?): BufferedReader?

expect fun stderrChild(child: COpaquePointer?): BufferedReader?
