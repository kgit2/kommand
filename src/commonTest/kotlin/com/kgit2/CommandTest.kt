package com.kgit2

import kotlin.test.Test

class CommandTest {
    @Test
    fun autoFree() {
        println(Command("echo").debugString())
    }
}
