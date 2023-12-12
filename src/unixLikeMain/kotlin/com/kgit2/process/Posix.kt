package com.kgit2.process

import com.kgit2.io.PlatformReader
import com.kgit2.io.PlatformWriter
import com.kgit2.io.Reader
import com.kgit2.io.Writer
import io.ktor.utils.io.errors.*
import kotlinx.cinterop.*
import platform.posix.*

object Posix {
    fun createWriter(file: CPointer<FILE>): Writer {
        return Writer(PlatformWriter(file))
    }

    fun createReader(file: CPointer<FILE>): Reader {
        return Reader(PlatformReader(file))
    }

    @Throws(IOException::class)
    fun pipe(fd: IntArray) {
        fd.usePinned { pin ->
            when (val result = platform.posix.pipe(pin.addressOf(0))) {
                EFAULT -> throw IOException("pipefd is not valid.")
                EINVAL -> throw IOException("Invalid flags.")
                EMFILE -> throw IOException("Too many open files")
                ENFILE -> throw IOException("The system limit on the total number of open files has been reached.")
                else -> Unit
            }
        }
    }

    @Throws(IOException::class)
    fun close(fileDescriptor: Int) {
        when (platform.posix.close(fileDescriptor)) {
            EBADF -> throw IOException("fd isn't a valid open file descriptor.")
            EINTR -> throw IOException("The close() call was interrupted by a signal.")
            EIO -> throw IOException("An I/O error occurred.")
            else -> Unit
        }
    }

    @Throws(IOException::class)
    fun dup2(oldFd: Int, newFd: Int) {
        when (platform.posix.dup2(oldFd, newFd)) {
            EBADF -> throw IOException("oldfd or newfd is not a valid file descriptor.")
            EBUSY -> throw IOException("(Linux only) This may be returned by dup2() or dup3() during a race condition with open(2) and dup().")
            EINTR -> throw IOException("The dup2() or dup3() call was interrupted by a signal; see signal(7).")
            EINVAL -> throw IOException("(dup3()) flags contain an invalid value. Or, oldfd was equal to newfd.")
            EMFILE -> throw IOException("The process already has the maximum number of file descriptors open and tried to open a new one.")
            else -> Unit
        }
    }

    @Throws(IOException::class)
    fun fork(): Int {
        return when (val pid = platform.posix.fork()) {
            EAGAIN -> throw IOException("fork() cannot allocate sufficient memory to copy the parent's page tables and allocate a task structure for the child.")
            ENOMEM -> throw IOException("fork() failed to allocate the necessary kernel structures because memory is tight.")
            ENOSYS -> throw IOException("fork() is not supported on this platform (for example, hardware without a Memory-Management Unit).")
            else -> pid
        }
    }

    @Throws(IOException::class)
    fun waitpid(pid: Int, options: Int): ChildExitStatus {
        return memScoped {
            val statusCode = alloc<IntVar>()
            when (val status = platform.posix.waitpid(pid, statusCode.ptr, options)) {
                ECHILD -> throw IOException("No child process with the specified pid exists.")
                EINTR -> throw IOException("The waitpid() call was interrupted by a signal.")
                EINVAL -> throw IOException("The options argument is not valid.")
                else -> status
            }
            ChildExitStatus(statusCode.value)
        }
    }

    @Throws(IOException::class)
    fun kill(pid: Int, signal: Int) {
        when (platform.posix.kill(pid, signal)) {
            EINVAL -> throw IOException("An invalid signal was specified.")
            EPERM -> throw IOException("The process does not have permission to send the signal to any of the target processes.")
            ESRCH -> throw IOException("The pid or process group does not exist. Note that an existing process might be a zombie, a process which already committed termination, but has not yet been wait(2)ed for.")
            else -> Unit
        }
    }

    @Throws(IOException::class)
    fun fdopen(fileDescriptor: Int, mode: String): CPointer<FILE> {
        return when (val file = platform.posix.fdopen(fileDescriptor, mode)) {
            null -> throw IOException("Invalid mode.")
            else -> file
        }
    }

    @Throws(IOException::class)
    fun chdir(path: String) {
        if (platform.posix.chdir(path) != 0) {
            when (platform.posix.errno) {
                EACCES -> throw IOException("Search permission is denied for one of the components of path.")
                EFAULT -> throw IOException("path points outside your accessible address space.")
                EIO -> throw IOException("An I/O error occurred.")
                ELOOP -> throw IOException("Too many symbolic links were encountered in resolving path.")
                ENAMETOOLONG -> throw IOException("path is too long.")
                ENOENT -> throw IOException("The directory specified in path does not exist.")
                ENOMEM -> throw IOException("Insufficient kernel memory was available.")
                ENOTDIR -> throw IOException("A component of the path prefix is not a directory.")
                EBADF -> throw IOException("fd is not a valid file descriptor.")
                else -> Unit
            }
        }
    }

    @Throws(IOException::class)
    fun execvp(commands: List<String?>) {
        memScoped {
            platform.posix.execvp(commands[0], allocArrayOf(commands.map { it?.cstr?.getPointer(memScope) }))
            when (errno) {
                E2BIG -> IOException("The total number of bytes in the environment (envp) and argument list (argv) is too large.")

                EACCES -> IOException("The file or a script interpreter is not a regular file.\nOr Execute permission is denied for the file or a script or ELF interpreter.\nOr The file system is mounted noexec.")

                EFAULT -> IOException("filename points outside your accessible address space.")

                EINVAL -> IOException("An ELF executable had more than one PT_INTERP segment (i.e., tried to name more than one interpreter).")

                EIO -> IOException("An I/O error occurred.")

                EISDIR -> IOException("An ELF interpreter was a directory.")

                ELOOP -> IOException("Too many symbolic links were encountered in resolving filename or the name of a script or ELF interpreter.")

                EMFILE -> IOException("The process has the maximum number of files open.")

                ENAMETOOLONG -> IOException("filename is too long.")

                ENFILE -> IOException("The system limit on the total number of open files has been reached.")

                ENOENT -> IOException("The file filename or a script or ELF interpreter does not exist, or a shared library needed for file or interpreter cannot be found.")

                ENOEXEC -> IOException("An executable is not in a recognized format, is for the wrong architecture, or has some other format error that means it cannot be executed.")

                ENOMEM -> IOException("Insufficient kernel memory was available.")

                ENOTDIR -> IOException("A component of the path prefix of filename or a script or ELF interpreter is not a directory.")

                EPERM -> IOException("The file system is mounted nosuid, the user is not the superuser, and the file has the set-user-ID or set-group-ID bit set.")

                ETXTBSY -> IOException("Executable was open for writing by one or more processes.")

                else -> Unit
            }
        }
    }
}
