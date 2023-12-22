package com.kgit2

import kommand_core.VoidResult
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CValue
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed

actual class Child(
    private val inner: COpaquePointer?
) {

    companion object {
        @Throws(KommandException::class)
        fun from(result: CValue<VoidResult>): Child = memScoped {
            if (result.ptr.pointed.ok != null) {
                return Child(result.ptr.pointed.ok)
            } else {
                println(result.ptr.pointed.error_type)
                throw KommandException(
                    result.ptr.pointed.err?.asString(),
                    result.ptr.pointed.error_type.to()
                )
            }
        }
    }
}
