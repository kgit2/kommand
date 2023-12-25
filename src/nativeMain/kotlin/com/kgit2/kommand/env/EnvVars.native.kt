package com.kgit2.kommand.env

actual fun envVar(key: String): String? {
    return com.kgit2.kommand.wrapper.envVar(key)
}

actual fun envVars(): Map<String, String>? {
    return com.kgit2.kommand.wrapper.envVars()
}
