package com.kgit2.process

import com.kgit2.exception.KommandException
import com.kgit2.io.Output

expect class Command(command: String) {
    val command: String

    fun debugString(): String

    fun arg(arg: String): Command

    fun args(args: List<String>): Command

    fun env(key: String, value: String): Command

    fun envs(envs: Map<String, String>): Command

    fun removeEnv(key: String): Command

    fun envClear(): Command

    fun cwd(dir: String): Command

    fun stdin(stdio: Stdio): Command

    fun stdout(stdio: Stdio): Command

    fun stderr(stdio: Stdio): Command

    @Throws(KommandException::class)
    fun spawn(): Child

    @Throws(KommandException::class)
    fun output(): Output

    @Throws(KommandException::class)
    fun status(): Int
}

enum class Stdio {
    Inherit,
    Pipe,
    Null,
    ;
    companion object;
}
