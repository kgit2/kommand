package com.kgit2.io

data class Output(
    val status: Int?,
    val stdout: String?,
    val stderr: String?,
) {
    companion object;
}
