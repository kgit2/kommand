package com.kgit2.env

actual fun envVar(key: String): String? {
    return System.getenv(key)
}

actual fun envVars(): Map<String, String>? {
    return System.getenv()
}
