package com.kgit2.wrapper

import com.kgit2.exception.KommandException
import com.kgit2.io.Output
import com.kgit2.process.Child
import com.kgit2.process.Stdio
import kommand_core.arg_command
import kommand_core.current_dir_command
import kommand_core.debug_command
import kommand_core.display_command
import kommand_core.drop_command
import kommand_core.env_clear_command
import kommand_core.env_command
import kommand_core.new_command
import kommand_core.output_command
import kommand_core.remove_env_command
import kommand_core.spawn_command
import kommand_core.status_command
import kommand_core.stderr_command
import kommand_core.stdin_command
import kommand_core.stdout_command
import kotlinx.cinterop.COpaquePointer

actual fun newCommand(command: String): COpaquePointer? {
    return new_command(command)
}

actual fun dropCommand(command: COpaquePointer?) {
    return drop_command(command)
}

actual fun displayCommand(command: COpaquePointer?): String? {
    return display_command(command)?.asString()
}

actual fun debugCommand(command: COpaquePointer?): String? {
    return debug_command(command)?.asString()
}

actual fun argCommand(command: COpaquePointer?, arg: String) {
    return arg_command(command, arg)
}

actual fun envCommand(command: COpaquePointer?, key: String, value: String) {
    return env_command(command, key, value)
}

actual fun removeEnvCommand(command: COpaquePointer?, key: String) {
    return remove_env_command(command, key)
}

actual fun envClearCommand(command: COpaquePointer?) {
    return env_clear_command(command)
}

actual fun currentDirCommand(command: COpaquePointer?, dir: String) {
    return current_dir_command(command, dir)
}

actual fun stdinCommand(command: COpaquePointer?, stdio: Stdio) {
    stdin_command(command, stdio.to())
}

actual fun stdoutCommand(command: COpaquePointer?, stdio: Stdio) {
    stdout_command(command, stdio.to())
}

actual fun stderrCommand(command: COpaquePointer?, stdio: Stdio) {
    stderr_command(command, stdio.to())
}

@Throws(KommandException::class)
actual fun spawnCommand(command: COpaquePointer?): Child = run {
    Child.from(spawn_command(command))
}

@Throws(KommandException::class)
actual fun outputCommand(command: COpaquePointer?): Output = run {
    Output.from(output_command(command))
}

@Throws(KommandException::class)
actual fun statusCommand(command: COpaquePointer?): Int = run {
    Int.from(status_command(command))
}
