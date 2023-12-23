package com.kgit2

import kotlin.test.Test

class EnvTest {
    @Test
    fun envVar() {
        com.kgit2.env.envVar("HOME")
    }

    @Test
    fun envVars() {
        com.kgit2.env.envVars()
    }
}
