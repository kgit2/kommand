package com.kgit2.wrapper

import kommand_core.drop_env_vars
import kommand_core.env_var
import kommand_core.env_vars
import kotlinx.cinterop.convert
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.toKString

actual fun envVar(name: String): String? {
    return env_var(name)?.asString()
}

actual fun envVars(): Map<String, String> = memScoped {
    val envVars = env_vars()
    val map = mutableMapOf<String, String>()
    val length = envVars.ptr.pointed.len
    val envVarsValue = envVars.ptr.pointed
    (0UL until length).forEach { i ->
        val name = envVarsValue.names?.get(i.convert())?.toKString()
        val value = envVarsValue.values?.get(i.convert())?.toKString()
        if (name != null && value != null) {
            map[name] = value
        }
    }
    drop_env_vars(envVars)
    map
}
