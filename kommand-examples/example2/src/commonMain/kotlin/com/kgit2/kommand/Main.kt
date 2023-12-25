package com.kgit2.kommand

import com.kgit2.kommand.process.Command
import com.kgit2.kommand.process.Stdio

fun main() {
    val child = Command("ping")
        .args(listOf("-c", "5", "localhost"))
        .stdout(Stdio.Pipe)
        .spawn()
    child.bufferedStdout()?.lines()?.forEach { line ->
        println(line)
    }
    child.wait()
}
