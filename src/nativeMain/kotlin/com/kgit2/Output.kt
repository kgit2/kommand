package com.kgit2

// import kommand_core.drop_output
// import kommand_core.into_output
// import kotlinx.cinterop.CValue
// import kotlinx.cinterop.memScoped
// import kotlinx.cinterop.pointed
// import kotlinx.cinterop.toKString
//
// fun Output.Companion.from(result: CValue<kommand_core.VoidResult>): Output = memScoped {
//     if (result.ptr.pointed.err != null) {
//         val errPtr = result.ptr.pointed.err!!
//         throw KommandException(errPtr.asString(), result.ptr.pointed.error_type.to())
//     } else {
//         val output = into_output(result.ptr.pointed.ok)
//         val newOutput = Output(
//             output.getPointer(memScope).pointed.exit_code,
//             output.getPointer(memScope).pointed.stdout_content?.toKString(),
//             output.getPointer(memScope).pointed.stderr_content?.toKString(),
//         )
//         drop_output(output)
//         newOutput
//     }
// }
