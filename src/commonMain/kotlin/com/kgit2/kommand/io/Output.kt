package com.kgit2.kommand.io

data class Output(
    val status: Int?,
    val stdout: String?,
    val stderr: String?,
) {
    companion object;
}
