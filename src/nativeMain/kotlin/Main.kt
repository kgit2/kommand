import com.floater.process.Command
import com.floater.process.Stdio

fun main() {
    val child = Command("myecho")
        .stdin(Stdio.Pipe)
        // .stdout(Stdio.Pipe)
        // .stderr(Stdio.Pipe)
        .spawn()
    val output = child.wait()
    println(output)
}
