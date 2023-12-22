package com.kgit2

class KommandException(
    message: String?,
    val errorType: ErrorType?,
) : Exception(message)

enum class ErrorType {
    None,
    IO,
    Utf8,
    Unknown,
    ;
    companion object
}
