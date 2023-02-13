import java.util.Scanner

fun main(args: Array<String>) {
    when (args[0]) {
        "echo" -> echo()
        "error" -> error()
        "interval" -> interval(args[1].toInt())
        else -> println("Unknown command: ${args[0]}")
    }
}

fun echo() {
    val scanner = Scanner(System.`in`)
    while (scanner.hasNextLine()) {
        val line = scanner.nextLine()
        println(line)
    }
}

fun error() {
    val scanner = Scanner(System.`in`)
    while (scanner.hasNextLine()) {
        val line = scanner.nextLine()
        System.err.println(line)
    }
}

fun interval(count: Int) {
    repeat(count) {
        println(it)
        Thread.sleep(1000)
    }
}
