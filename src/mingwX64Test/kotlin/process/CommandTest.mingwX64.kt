package process

import kotlinx.cinterop.UShortVar
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.convert
import kotlinx.cinterop.get
import kotlinx.cinterop.invoke
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.set
import platform.windows.GetEnvironmentVariable

actual val eko: String = "eko/target/release/eko"

actual fun shellTest() {}

actual fun envVar(key: String): String? {
    memScoped {
        val lpSize: UInt = 10240u
        val lpBuffer = allocArray<UShortVar>(10240)
        val lpName = allocArray<UShortVar>(key.length.convert())
        key.forEachIndexed { index, c ->
            lpName[index] = c.code.toUShort()
        }
        val size = GetEnvironmentVariable!!.invoke(lpName, lpBuffer, lpSize)
        if (size == 0u) {
            return null
        } else {
            val buffer = CharArray(size.toInt())
            for (i in 0 until size.toInt()) {
                buffer[i] = lpBuffer[i].toInt().toChar()
            }
            return buffer.joinToString("")
        }
    }
}

actual fun homeDir(): String? {
    return envVar("HOME")
}
