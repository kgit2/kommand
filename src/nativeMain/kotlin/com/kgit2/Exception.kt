package com.kgit2

fun kommand_core.ErrorType.to(): ErrorType {
    return when (this) {
        kommand_core.ErrorType.None -> ErrorType.None
        kommand_core.ErrorType.Io -> ErrorType.IO
        kommand_core.ErrorType.Utf8 -> ErrorType.Utf8
        kommand_core.ErrorType.Unknown -> ErrorType.Unknown
    }
}
