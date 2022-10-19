import com.floater.process.Command
import com.floater.process.Stdio
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.*

fun main() {
    // val child = Command("myecho")
    //     .stdin(Stdio.Pipe)
    //     // .stdout(Stdio.Pipe)
    //     // .stderr(Stdio.Pipe)
    //     .spawn()
    // val output = child.wait()
    // println(output)

    val writer = BytePacketBuilder()
    val reader = writer.build()
    writer.appendLine("Hello, world!")
    println(reader.readUTF8Line())
}
