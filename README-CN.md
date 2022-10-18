# Kommand
一个可以将外部命令跑在子进程的库，用于Kotlin Native/JVM

# Dependent
- 深受rust-std command启发。
- 基于ktor-client所提供的io，可使用管道进行进程间通信。

## Native for macOS/Linux/Mingw
- 使用POSIX api的系统调用

## JVM
- 基于 `ProcessBuilder`

# Status
该库目前仍处于开发中不可用状态
