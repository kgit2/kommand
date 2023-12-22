package com.kgit2.wrapper

import com.kgit2.Child
import com.kgit2.Output
import com.kgit2.Stdio
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

expect fun spawnCommand(command: COpaquePointer?): Child

expect fun outputCommand(command: COpaquePointer?): Output

expect fun statusCommand(command: COpaquePointer?): Int?
