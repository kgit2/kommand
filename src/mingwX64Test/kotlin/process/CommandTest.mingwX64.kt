package process

import platform.windows.GetEnvironmentStrings
import platform.windows.GetEnvironmentVariable
import platform.windows.GetEnvironmentVariableA

actual val eko: String = "eko/target/release/eko"

// actual val subCommand: String = "sub_command\\build\\install\\sub_command\\bin\\sub_command.bat"

actual fun shellTest() {}

actual fun envVar(key: String): String? {
    // GetEnvironmentVariableA()
    TODO()
}

actual fun tempDir(): String? {
    TODO("Not yet implemented")
}
