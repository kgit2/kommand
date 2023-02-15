package process

actual val subCommand: String = "sub_command\\build\\install\\sub_command\\bin\\sub_command.bat"

actual fun shellTest() {
    // windows has no sh/bash shell

    // val output = Command("sh")
    //     .args("-c", "f() { echo username=a; echo password=b; }; f get")
    //     .stdout(Stdio.Pipe)
    //     .spawn()
    //     .waitWithOutput()
    // assertEquals("username=a\npassword=b\n", output)
}
