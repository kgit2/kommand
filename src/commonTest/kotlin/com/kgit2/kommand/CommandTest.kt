package com.kgit2.kommand

import com.kgit2.kommand.process.Command
import com.kgit2.kommand.process.Stdio
import kotlin.test.Test
import kotlin.test.assertEquals

class CommandTest {
    @Test
    fun autoFree() {
        Command("echo").debugString()
    }

    @Test
    fun simpleEcho() {
        val command = Command(platformEchoPath())
        val output = command.output()
        assertEquals("Hello, Kommand!", output.stdout)
        assertEquals("Hello, Kommand!", command.output().stdout)
        assertEquals("Hello, Kommand!", command.output().stdout)
        assertEquals("Hello, Kommand!", command.output().stdout)
        val status = command.status()
        assertEquals(0, status)
        assertEquals(0, command.status())
        assertEquals(0, command.status())
        assertEquals(0, command.status())
    }

    @Test
    fun discardIO() {
        val command = Command(platformEchoPath()).stdout(Stdio.Null)
        val output = command.output()
        assertEquals("", output.stdout)
        val status = command.status()
        assertEquals(0, status)
    }

    @Test
    fun colorEcho() {
        val command = Command(platformEchoPath()).arg("color")
        val output = command.output()
        val expect = listOf(
            "\u001B[31mHello, Kommand!\u001B[0m",
            "\u001B[32mHello, Kommand!\u001B[0m",
            "\u001B[34mHello, Kommand!\u001B[0m",
        ).joinToString("\n") + "\n"
        assertEquals(expect, output.stdout)
        val status = command.status()
        assertEquals(0, status)
    }

    @Test
    fun currentWorkingDirectory() {
        val command = platformCwd().cwd(homeDir())
        val output = command.output()
        assertEquals(homeDir(), output.stdout?.trim())
        val status = command.status()
        assertEquals(0, status)
    }
}
