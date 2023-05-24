package process
import com.kgit2.process.Command
import com.kgit2.process.Stdio
import kotlin.test.Test
import kotlin.test.assertTrue

class CommandTest {
    fun outputWithExitCodeTest() {
        @Test
        fun tempTest() {
            val executor = Command("ping")
                .arg("-c")
                .args("5", "localhost")
                .stdout(Stdio.Pipe)
                .spawn()
            val stdoutReader = executor.getChildStdout()!!
            val sb = StringBuilder()
            stdoutReader.lines().forEach {
                // do something
                sb.appendLine(it)
            }
            val exitStatus = runCatching {
                executor.wait()
            }
            assertTrue(exitStatus.isSuccess)
        }
    }

    @Test
    fun pipeTest() {
        val child1 = Command("zsh")
            .args("-c", "unset count; count=0; while ((count < 10)) do ((count += 1));echo from child1:${"$"}count;done")
            .stdout(Stdio.Pipe)
            .spawn()
        val child2 = Command("zsh")
            .args("-c", "while read line; do echo from child2:${"$"}line; done")
            .stdin(Stdio.Pipe)
            .stdout(Stdio.Inherit)
            .spawn()
        val child1StdoutReader = child1.getChildStdout()!!
        val child2StdinWriter = child2.getChildStdin()!!
        child1StdoutReader.lines().forEach {
            child2StdinWriter.appendLine(it)
        }
        child2StdinWriter.close()
    }
}
