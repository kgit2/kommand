import com.floater.process.Command
import com.floater.process.Stdio
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.*
import platform.posix.stdin
import platform.posix.stdout

fun main() = runBlocking<Unit> {
    val child = Command("ping")
        .args("-c 5 localhost")
        // .stdin(Stdio.Pipe)
        .stdout(Stdio.Pipe)
        // .stderr(Stdio.Pipe)
        .spawn(1L)
    val stdin = child.getChildStdin()
    val stdout = child.getChildStdout()
    // the canRead() method is not working before calling endOfInput()
    // while (stdout?.endOfInput == false) {
    //     val line = stdout.readUTF8Line()
    //     println("stdout: $line")
    // }
    withContext(Dispatchers.Default) {
        println("will launch write")
        launch {
            println("launch write")
            for (i in 1..10) {
                stdin?.appendLine("hello$i")
                stdin?.flush()
            }
            stdin?.close()
        }
        println("will launch read")
        launch {
            println("launch read")
            stdout?.lines()?.forEach {
                println("stdout: $it")
            }
            println("stdout read finished")
            stdout?.close()
        }
    }
    // child.wait()
}
