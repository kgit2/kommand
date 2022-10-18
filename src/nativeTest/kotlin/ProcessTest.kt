import com.floater.process.Command
import com.floater.process.Stdio
import kotlin.test.Test

class ProcessTest {
    @Test
    fun ls() {
        val output = Command("ls")
            .arg("-al")
            .stdout(Stdio.Null)
            .spawn()
            .waitWithOutput()
        println(output)
    }

    @Test
    fun echo() {
        val child = Command("ls")
            .arg("myecho")
            // .stdin(Stdio.Pipe)
            // .stdout(Stdio.Pipe)
            // .stderr(Stdio.Pipe)
            .spawn()
        val output = child.wait()
        println(output)
    }
}
