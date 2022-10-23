package com.floater.process

import com.floater.io.Reader
import com.floater.io.Writer
import io.ktor.utils.io.core.*
import io.ktor.utils.io.errors.*

expect class Child(
    command: String,
    args: List<String>,
    envs: Map<String, String>,
    cwd: String? = null,
    stdin: Stdio,
    stdout: Stdio,
    stderr: Stdio,
) {
    val command: String
    val args: List<String>
    val envs: Map<String, String>
    val cwd: String?
    val stdin: Stdio
    val stdout: Stdio
    val stderr: Stdio

    var id: Int?
    fun getChildStdin(): Writer?
    fun getChildStdout(): Reader?
    fun getChildStderr(): Reader?
    @Throws(IOException::class)
    fun start(options: ChildOptions = ChildOptions.W_NOHANG)
    @Throws(IOException::class)
    fun wait(): ChildExitStatus
    @Throws(IOException::class)
    fun waitWithOutput(): String?
    fun kill()
}

class ChildOptions private constructor(
    val value: Int
) {
    companion object {
        val W_UNTRACED = ChildOptions(0x00000001)
        val W_NOHANG = ChildOptions(0x00000002)
    }

    infix fun or(other: ChildOptions): ChildOptions {
        return ChildOptions(this.value or other.value)
    }

    operator fun contains(other: ChildOptions): Boolean {
        return this.value and other.value != 0
    }
}

data class ChildExitStatus(
    val code: Int,
) {
    inline fun w_stopped(): Int {
        return 0x7F
    }

    inline fun w_coreflag(): Int {
        return 0x80
    }

    inline fun w_status(x: Int): Int {
        return x and w_stopped()
    }

    inline fun w_stopsig(x: Int): Int {
        return x shr 8
    }
    /**
     * returns true if the child terminated normally,
     * that is, by calling exit(3) or _exit(2),
     * or by returning from main().
     */
    inline fun exited(): Boolean {
        return w_status(code) == 0
    }
    /**
     * returns the exit status of the child.
     * This consists of the least significant 8 bits of the status argument
     * that the child specified in a call to exit(3) or _exit(2)
     * or as the argument for a return statement in main().
     * This macro should only be employed if WIFEXITED returned true.
     */
    inline fun exitStatus(): Int {
        return w_stopsig(code) and 0x000000FF
    }
    /**
     * returns true if the child process was terminated by a signal.
     */
    inline fun signaled(): Boolean {
        return (w_status(code) != w_stopped()) && (w_status(code) != 0)
    }
    /**
     * returns the number of the signal that caused the child process to terminate.
     * This macro should only be employed if WIFSIGNALED returned true.
     */
    inline fun signal(): Int {
        return w_status(code)
    }
    /**
     * returns true if the child produced a core dump.
     * This macro should only be employed if WIFSIGNALED returned true.
     * This macro is not specified in POSIX.1-2001
     * and is not available on some UNIX implementations (e.g., AIX, SunOS).
     * Only use this enclosed in #ifdef WCOREDUMP ... #endif.
     */
    inline fun coreDump(): Boolean {
        return code == w_coreflag()
    }
    /**
     * returns true if the child process was stopped by delivery of a signal;
     * this is only possible if the call was done using WUNTRACED or when the child is being traced (see ptrace(2)).
     */
    inline fun stopped(): Boolean {
        return (w_status(code) == w_stopped()) && (w_stopsig(code) != 0x13)
    }
    /**
     * returns the number of the signal which caused the child to stop.
     * This macro should only be employed if WIFSTOPPED returned true.
     */
    inline fun stopSignal(): Int {
        return w_stopsig(code)
    }
}
