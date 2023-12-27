package com.kgit2.kommand

import com.kgit2.kommand.process.Command
import com.kgit2.kommand.process.Stdio

fun main() {
    Command("echo")
        .arg("nothing")
        .stdout(Stdio.Null)
        .spawn()
        .wait()
}
