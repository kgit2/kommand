package com.kgit2.env

actual fun envVar(key: String): String? {
    return com.kgit2.wrapper.envVar(key)
}

actual fun envVars(): Map<String, String>? {
    return com.kgit2.wrapper.envVars()
}
