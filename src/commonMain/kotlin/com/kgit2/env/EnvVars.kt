package com.kgit2.env

expect fun envVar(key: String): String?

expect fun envVars(): Map<String, String>?
