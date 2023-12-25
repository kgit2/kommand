package com.kgit2.kommand.wrapper

expect fun envVar(name: String): String?

expect fun envVars(): Map<String, String>
