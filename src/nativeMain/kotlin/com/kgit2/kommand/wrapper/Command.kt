package com.kgit2.kommand.wrapper

import com.kgit2.kommand.exception.KommandException
import com.kgit2.kommand.io.Output
import com.kgit2.kommand.process.Child
import com.kgit2.kommand.process.Stdio
import kotlinx.cinterop.COpaquePointer

expect fun newCommand(command: String): COpaquePointer?

expect fun dropCommand(command: COpaquePointer?)

expect fun displayCommand(command: COpaquePointer?): String?

expect fun debugCommand(command: COpaquePointer?): String?

expect fun argCommand(command: COpaquePointer?, arg: String)

expect fun envCommand(command: COpaquePointer?, key: String, value: String)

expect fun removeEnvCommand(command: COpaquePointer?, key: String)

expect fun envClearCommand(command: COpaquePointer?)

expect fun currentDirCommand(command: COpaquePointer?, dir: String)

expect fun stdinCommand(command: COpaquePointer?, stdio: Stdio)

expect fun stdoutCommand(command: COpaquePointer?, stdio: Stdio)

expect fun stderrCommand(command: COpaquePointer?, stdio: Stdio)

@Throws(KommandException::class)
expect fun spawnCommand(command: COpaquePointer?): Child

@Throws(KommandException::class)
expect fun outputCommand(command: COpaquePointer?): Output

@Throws(KommandException::class)
expect fun statusCommand(command: COpaquePointer?): Int
