package com.kgit2

import kommand_core.drop_string
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.toKString

inline fun CPointer<ByteVar>.asString(): String {
    val result = this.toKString()
    drop_string(this)
    return result
}
