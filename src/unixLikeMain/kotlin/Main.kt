import com.kgit2.process.Command
import com.kgit2.process.Stdio
import kotlinx.coroutines.*

fun main() = runBlocking<Unit> {
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
