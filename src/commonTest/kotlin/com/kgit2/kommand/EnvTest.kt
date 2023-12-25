package com.kgit2.kommand

import kotlin.test.Test

class EnvTest {
    @Test
    fun envVar() {
        com.kgit2.kommand.env.envVar("HOME")
    }

    @Test
    fun envVars() {
        com.kgit2.kommand.env.envVars()
    }
}
