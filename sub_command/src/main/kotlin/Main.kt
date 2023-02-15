import java.util.Scanner

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        print("Hello, Kommand!\n")
        return
    }
    when (args[0]) {
        "echo" -> echo()
        "error" -> error()
        "interval" -> interval(if (args.size > 1) args[1].toInt() else null)
        else -> System.err.println("Unknown command: ${args[0]}")
    }
}

fun echo() {
    val scanner = Scanner(System.`in`)
    while (scanner.hasNext()) {
        val line = scanner.nextLine()
        print("$line\n")
    }
}

fun error() {
    val scanner = Scanner(System.`in`)
    while (scanner.hasNext()) {
        val line = scanner.nextLine()
        System.err.print("$line\n")
    }
}

fun interval(count: Int?) {
    repeat(count ?: 5) {
        print("$it\n")
        Thread.sleep(100)
    }
}
