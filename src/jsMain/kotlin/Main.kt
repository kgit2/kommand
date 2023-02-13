@file:Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")

import child_process.ChildProcess
import child_process.ChildProcessByStdio

fun main() {
    val module = js("require('child_process')")
    val options: dynamic = js("{stdio: [0, 1, 2]}")
    val child: ChildProcess = module.spawn("ls", arrayOf("-l"), options) as ChildProcess
    console.log(child)
    // process.spawnargs()
    // console.log(process)
}
