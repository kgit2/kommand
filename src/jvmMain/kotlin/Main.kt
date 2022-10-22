import com.floater.process.Command
import com.floater.process.Stdio
import io.ktor.utils.io.core.*
import java.io.BufferedWriter
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.PipedInputStream

fun main() {
    val child = Command("ping")
        .args("-c 5 localhost")
        .stdout(Stdio.Pipe)
        .spawn()
    // val output = child.waitWithOutput()
    // println(output)
    // println("=============================================")
    // println(output?.length)
    var length = 0
    child.getChildStdout()?.lines()?.forEach {
        println(it)
        length += it.length
    }
    println("=============================================")
    println(length)

}
