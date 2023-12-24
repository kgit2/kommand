package com.kgit2.wrapper

expect fun envVar(name: String): String?

expect fun envVars(): Map<String, String>
