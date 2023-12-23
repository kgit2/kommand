package com.kgit2

import com.kgit2.process.Command
import kotlin.test.Test

class CommandTest {
    @Test
    fun autoFree() {
        println(Command("echo").debugString())
    }
}
