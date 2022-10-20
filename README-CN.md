# Kommand
一个可以将外部命令跑在子进程的库，用于Kotlin Native/JVM

# Dependent
- 深受rust-std `Command`启发。
- 基于ktor-io，可以使用管道处理进程间通信(IPC)。
- kotlin多平台1.7.20，使用新的内存管理器。

## Native for macOS/Linux/Mingw
- 使用POSIX api的系统调用

## JVM
- 基于 `java.lang.ProcessBuilder`

# Status
该库目前仍处于开发中不可用状态
