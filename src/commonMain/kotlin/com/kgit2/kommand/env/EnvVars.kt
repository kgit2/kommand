package com.kgit2.kommand.env

expect fun envVar(key: String): String?

expect fun envVars(): Map<String, String>?
