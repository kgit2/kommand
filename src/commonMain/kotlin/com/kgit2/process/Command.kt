package com.kgit2.process

public class Command(
    val command: String
) {
    private val args = mutableListOf<String>()
    private val envs = mutableMapOf<String, String>()
    private var cwd: String? = null

    private var stdin: Stdio = Stdio.Inherit
    private var stdout: Stdio = Stdio.Inherit
    private var stderr: Stdio = Stdio.Inherit

    public fun arg(arg: String): Command {
        this.args.addAll(arg.split(" "))
        return this
    }

    public fun args(vararg args: String): Command {
        this.args.addAll(args)
        return this
    }

    public fun env(key: String, value: String): Command {
        this.envs[key] = value
        return this
    }

    public fun envs(vararg envs: Pair<String, String>): Command {
        this.envs.putAll(envs)
        return this
    }

    public fun cwd(cwd: String): Command {
        this.cwd = cwd
        return this
    }


    public fun stdin(stdin: Stdio): Command {
        this.stdin = stdin
        return this
    }

    public fun stdout(io: Stdio): Command {
        this.stdout = io
        return this
    }

    public fun stderr(io: Stdio): Command {
        this.stderr = io
        return this
    }

    public fun spawn(): Child {
        val child = Child(
            command = command,
            args = args,
            envs = envs,
            cwd = cwd,
            stdin = stdin,
            stdout = stdout,
            stderr = stderr,
        )
        child.start()
        return child
    }

    public fun output(): String? {
        return spawn().waitWithOutput()
    }

    public fun status(): ChildExitStatus {
        return spawn().wait()
    }

    public fun getArgs(): List<String> {
        return args
    }

    public fun getEnvs(): Map<String, String> {
        return envs
    }

    public fun getCwd(): String? {
        return cwd
    }

    override fun toString(): String {
        return "Command(command='$command', args=$args, envs=$envs, cwd=$cwd, stdin=$stdin, stdout=$stdout, stderr=$stderr)"
    }

    public fun prompt(): String {
        return "$command ${args.joinToString(" ")}"
    }
}


enum class Stdio {
    Inherit,
    Pipe,
    Null,
}
