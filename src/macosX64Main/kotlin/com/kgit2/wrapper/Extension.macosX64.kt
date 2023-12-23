package com.kgit2.wrapper

import com.kgit2.exception.ErrorType
import com.kgit2.exception.KommandException
import com.kgit2.io.Output
import com.kgit2.process.Child
import com.kgit2.process.Stdio
import kommand_core.drop_output
import kommand_core.drop_string
import kommand_core.into_output
import kommand_core.void_to_string
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValue
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.toKString

inline fun CPointer<ByteVar>.asString(): String {
    val result = this.toKString()
    drop_string(this)
    return result
}

@Throws(KommandException::class)
fun Child.Companion.from(result: CValue<kommand_core.VoidResult>): Child = memScoped {
    if (result.ptr.pointed.ok != null) {
        Child(result.ptr.pointed.ok)
    } else if (result.ptr.pointed.err != null) {
        throw KommandException(
            result.ptr.pointed.err?.asString(),
            result.ptr.pointed.error_type.to()
        )
    } else {
        throw KommandException("[spawn_command] return [result]'s [ok] & [err] are both null", ErrorType.Unknown)
    }
}

@Throws(KommandException::class)
fun Output.Companion.from(result: CValue<kommand_core.VoidResult>): Output = memScoped {
    if (result.ptr.pointed.err != null) {
        val errPtr = result.ptr.pointed.err!!
        throw KommandException(errPtr.asString(), result.ptr.pointed.error_type.to())
    } else {
        val output = into_output(result.ptr.pointed.ok)
        val newOutput = Output(
            output.getPointer(memScope).pointed.exit_code,
            output.getPointer(memScope).pointed.stdout_content?.toKString(),
            output.getPointer(memScope).pointed.stderr_content?.toKString(),
        )
        drop_output(output)
        newOutput
    }
}

@Throws(KommandException::class)
fun Int.Companion.from(result: CValue<kommand_core.IntResult>): Int = memScoped {
    if (result.ptr.pointed.err != null) {
        val errPtr = result.ptr.pointed.err!!
        throw KommandException(errPtr.asString(), result.ptr.pointed.error_type.to())
    } else {
        return result.ptr.pointed.ok
    }
}

@Throws(KommandException::class)
fun String.Companion.from(result: CValue<kommand_core.VoidResult>): String? = memScoped {
    if (result.ptr.pointed.err != null) {
        val errPtr = result.ptr.pointed.err!!
        throw KommandException(errPtr.asString(), result.ptr.pointed.error_type.to())
    } else {
        return void_to_string(result.ptr.pointed.ok)?.asString()
    }
}

@Throws(KommandException::class)
fun CValue<kommand_core.UnitResult>.unwrap() = memScoped {
    if (this@unwrap.ptr.pointed.err != null) {
        val errPtr = this@unwrap.ptr.pointed.err!!
        throw KommandException(errPtr.asString(), this@unwrap.ptr.pointed.error_type.to())
    }
}

fun Stdio.to(): kommand_core.Stdio {
    return when (this) {
        Stdio.Inherit -> kommand_core.Stdio.Inherit
        Stdio.Pipe -> kommand_core.Stdio.Pipe
        Stdio.Null -> kommand_core.Stdio.Null
    }
}

fun kommand_core.ErrorType.to(): ErrorType {
    return when (this) {
        kommand_core.ErrorType.None -> ErrorType.None
        kommand_core.ErrorType.Io -> ErrorType.IO
        kommand_core.ErrorType.Utf8 -> ErrorType.Utf8
        kommand_core.ErrorType.Unknown -> ErrorType.Unknown
    }
}
