import io.ktor.utils.io.core.*
import java.io.BufferedWriter
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.PipedInputStream

fun main() {
    // val child = Command("myecho")
    //     .stdin(Stdio.Pipe)
    //     // .stdout(Stdio.Pipe)
    //     // .stderr(Stdio.Pipe)
    //     .spawn()
    // val output = child.wait()
    // println(output)

    ProcessBuilder("myecho")
        .redirectInput(ProcessBuilder.Redirect.PIPE)
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .redirectError(ProcessBuilder.Redirect.PIPE)
        .start()
        .apply {
        }
    val writer = BytePacketBuilder()
    val reader = writer.build()
    writer.appendLine("Hello, world!")
    println(reader.readUTF8Line())
}
