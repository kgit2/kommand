package com.kgit2.kommand

import com.kgit2.kommand.process.Command
import com.kgit2.kommand.process.Stdio

fun main() {
    Command("ping")
        .args(listOf("-c", "5", "localhost"))
        .stdout(Stdio.Inherit)
        .spawn()
        .wait()
}
