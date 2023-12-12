package process

import com.kgit2.process.Command
import com.kgit2.process.Stdio
import java.io.File
import java.util.*
import kotlin.test.assertEquals

enum class OS {
    WIN,
    MAC,
    LINUX,
}

val osName = System.getProperty("os.name").lowercase(Locale.getDefault())
val os: OS? = if (osName.contains("win")) {
    OS.WIN
} else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
    OS.LINUX
} else if (osName.contains("mac")) {
    OS.MAC
} else null

actual val eko: String =
    when (os) {
        OS.WIN -> "eko/target/release/eko.exe"
        else -> "eko/target/release/eko"
    }

// actual val subCommand: String =
//     when (os) {
//         OS.WIN -> "sub_command/build/install/sub_command/bin/sub_command.bat"
//         else -> "sub_command/build/install/sub_command/bin/sub_command"
//     }

actual fun shellTest() {
    when (os) {
        OS.WIN -> Unit
        else -> {
            val output = Command("sh")
                .args("-c", "f() { echo username=a; echo password=b; }; f get")
                .stdout(Stdio.Pipe)
                .spawn()
                .waitWithOutput()
            assertEquals("username=a\npassword=b\n", output)
        }
    }
}

actual fun envVar(key: String): String? {
    return System.getenv(key)
}

actual fun tempDir(): String? {
    return envVar("TMPDIR")
}
