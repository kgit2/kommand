import com.floater.process.Command
import com.floater.process.Stdio
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.*

fun main() {
    val child = Command("ls")
        .arg("-la")
        // .stdin(Stdio.Pipe)
        .stdout(Stdio.Pipe)
        // .stderr(Stdio.Pipe)
        .spawn()
    val stdout = child.getChildStdout()
    // the canRead() method is not working before calling endOfInput()
    while (stdout?.endOfInput == false) {
        val line = stdout.readUTF8Line()
        println("stdout: $line")
    }
}
