package process

import com.floater.process.Command
import com.floater.process.Stdio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import server.startServer
import kotlin.test.Test
import kotlin.test.assertEquals

class CommandTest {
    @Test
    fun curlCommand() = runBlocking<Unit> {
        val expectString = "Hello, world!"
        val port = 8080
        val result = withContext(Dispatchers.Default) {
            startServer(port, expectString)
        }
        val output = Command("curl")
            .args("-s", "http://localhost:${port}/")
            .stdout(Stdio.Pipe)
            .spawn()
            .waitWithOutput()
        assertEquals(expectString, output)
    }

    @Test
    fun pingCommand() {
        val output = Command("ping")
            .args("-c", "5", "localhost")
            .stdout(Stdio.Pipe)
            .spawn()
            .waitWithOutput()
        val expectLineCount = 11
        val lineCount = output?.lines()?.count()
        assertEquals(expectLineCount, lineCount)
    }

    @Test
    fun pingCommandForEachLine() {
        val expectLineCount = 10
        var lineCount = 0
        Command("ping")
            .args("-c", "5", "localhost")
            .stdout(Stdio.Pipe)
            .spawn()
            .getChildStdout()
            ?.lines()?.forEach {
                println(it)
                lineCount += 1
            }
        assertEquals(expectLineCount, lineCount)
    }

    @Test
    fun shTest() {
        val output = Command("sh")
            .args("-c", "f() { echo username=a; echo password=b; }; f get")
            .stdout(Stdio.Pipe)
            .spawn()
            .waitWithOutput()
        assertEquals("username=a\npassword=b\n", output)
    }
}
