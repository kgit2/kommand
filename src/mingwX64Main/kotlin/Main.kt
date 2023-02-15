import com.kgit2.process.Command
import com.kgit2.process.Stdio
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.LongVar
import kotlinx.cinterop.UShortVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.cValue
import kotlinx.cinterop.convert
import kotlinx.cinterop.invoke
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.set
import kotlinx.cinterop.sizeOf
import kotlinx.cinterop.toKString
import kotlinx.cinterop.value
import kotlinx.coroutines.runBlocking
import platform.posix._open_osfhandle
import platform.posix.exit
import platform.posix.fdopen
import platform.posix.fgets
import platform.posix.intptr_tVar
import platform.windows.CloseHandle
import platform.windows.CreatePipe
import platform.windows.CreateProcess
import platform.windows.GetLastError
import platform.windows.HANDLEVar
import platform.windows.HANDLE_FLAG_INHERIT
import platform.windows.INFINITE
import platform.windows.PROCESS_INFORMATION
import platform.windows.SECURITY_ATTRIBUTES
import platform.windows.STARTF_USESTDHANDLES
import platform.windows.STARTUPINFO
import platform.windows.SetHandleInformation
import platform.windows.WaitForSingleObject
import kotlin.test.assertEquals

// fun main(args: Array<String>) = runBlocking<Unit> {
//     memScoped {
//         println(args.joinToString("\n"))
//         val stdoutPipeReaderHandle: HANDLEVar = alloc()
//         val stdoutPipeWriterHandle: HANDLEVar = alloc()
//         val saAttr = cValue<SECURITY_ATTRIBUTES> {
//             nLength = sizeOf<SECURITY_ATTRIBUTES>().toUInt()
//             bInheritHandle = 1
//             lpSecurityDescriptor = null
//         }
//         CreatePipe(stdoutPipeReaderHandle.ptr, stdoutPipeWriterHandle.ptr, saAttr.ptr, 0)
//         SetHandleInformation(stdoutPipeReaderHandle.value, HANDLE_FLAG_INHERIT, 0)
//
//         // val piProcInfo = cValue<PROCESS_INFORMATION>()
//         val piProcInfo = alloc<PROCESS_INFORMATION>()
//         val siStartInfo = cValue<STARTUPINFO> {
//             cb = sizeOf<STARTUPINFO>().toUInt()
//             hStdOutput = stdoutPipeWriterHandle.value
//             dwFlags = dwFlags or STARTF_USESTDHANDLES.convert()
//         }
//         val temp = "C:\\Users\\bppleman\\Documents\\kgit2\\kommand\\sub_command\\build\\install\\sub_command\\bin\\sub_command.bat"
//         val cmdLine = allocArray<UShortVar>(temp.length.convert())
//         temp.withIndex().forEach {
//             cmdLine[it.index] = it.value.code.toUShort()
//         }
//         var bSuccess = CreateProcess!!.invoke(
//             null,
//             cmdLine,
//             null,          // process security attributes
//             null,          // primary thread security attributes
//             1,          // handles are inherited
//             0u,             // creation flags
//             null,          // use parent's environment
//             null,          // use parent's current directory
//             siStartInfo.ptr,  // STARTUPINFO pointer
//             piProcInfo.ptr
//         );  // receives PROCESS_INFORMATION
//         if (bSuccess == 0) {
//             println("CreateProcess failed (${GetLastError()}).");
//             exit(1);
//         } else {
//             // Close handles to the child process and its primary thread.
//             // Some applications might keep these handles to monitor the status
//             // of the child process, for example.
//
//             // CloseHandle(piProcInfo.hProcess);
//             // CloseHandle(piProcInfo.hThread);
//
//             // Close handles to the stdin and stdout pipes no longer needed by the child process.
//             // If they are not explicitly closed, there is no way to recognize that the child process has ended.
//             CloseHandle(stdoutPipeWriterHandle.value)
//             // WaitForSingleObject(hProcess, INFINITE)
//             // println(dwProcessId.toInt())
//         }
//
//         println(stdoutPipeReaderHandle.reinterpret<intptr_tVar>().value)
//         val handle: CPointer<HANDLEVar> = stdoutPipeReaderHandle.ptr
//         println("handle ${stdoutPipeReaderHandle.ptr.reinterpret<LongVar>().pointed.value}")
//         readFromFD(stdoutPipeReaderHandle)
//         WaitForSingleObject(piProcInfo.hProcess, INFINITE)
//         println(piProcInfo.dwProcessId.toInt())
//         CloseHandle(piProcInfo.hProcess)
//         CloseHandle(piProcInfo.hThread)
//     }
// }
//
// fun readFromFD(stdout_reader: HANDLEVar) {
//     val fd = _open_osfhandle(stdout_reader.reinterpret<intptr_tVar>().value, 0)
//     println("fd: $fd")
//     val file = fdopen(fd, "r")
//     memScoped {
//         val buf = allocArray<ByteVar>(4096)
//         while (true) {
//             val result = fgets(buf, 4096, file)
//             if (result != null) {
//                 print(result.toKString())
//             } else {
//                 break
//             }
//         }
//     }
// }

fun main() {
    println("begin")
    val expectString = "Hello, Kommand!"
    val child = Command("sub_command\\build\\install\\sub_command\\bin\\sub_command.bat")
        .args("error")
        .stdin(Stdio.Pipe)
        .stderr(Stdio.Pipe)
        .spawn()
    val writer = child.getChildStdin()!!
    writer.appendLine(expectString)
    writer.flush()
    writer.close()
    val reader = child.getChildStderr()!!
    val output = reader.readLine()
    assertEquals(expectString, output)
    child.wait()
    println("end")
}
