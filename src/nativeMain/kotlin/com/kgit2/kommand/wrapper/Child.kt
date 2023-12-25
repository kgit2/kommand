package com.kgit2.kommand.wrapper

import com.kgit2.kommand.exception.KommandException
import com.kgit2.kommand.io.BufferedReader
import com.kgit2.kommand.io.BufferedWriter
import com.kgit2.kommand.io.Output
import kotlinx.cinterop.COpaquePointer

expect fun idChild(child: COpaquePointer?): UInt

expect fun dropChild(child: COpaquePointer?)

@Throws(KommandException::class)
expect fun killChild(child: COpaquePointer?)

@Throws(KommandException::class)
expect fun waitChild(child: COpaquePointer?): Int

@Throws(KommandException::class)
expect fun waitWithOutputChild(child: COpaquePointer?): Output

expect fun bufferedStdinChild(child: COpaquePointer?): BufferedWriter?

expect fun bufferedStdoutChild(child: COpaquePointer?): BufferedReader?

expect fun bufferedStderrChild(child: COpaquePointer?): BufferedReader?
