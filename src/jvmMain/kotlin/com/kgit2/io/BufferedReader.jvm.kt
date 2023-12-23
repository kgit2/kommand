package com.kgit2.io

actual class BufferedReader {
    actual fun readLine(): String? {
        return ""
    }

    actual fun readAll(): String? {
        return ""
    }
}
