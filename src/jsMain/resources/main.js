(async () => {
    var child_process = require('child_process');
    console.log(child_process)
    let process = child_process.spawn('../../../sub_command/build/install/sub_command/bin/sub_command', ["echo"], {stdio: ['pipe', 'pipe', 'inherit']})
    console.log(process)
    process.stdin.write("Hello World1\n")
    process.stdin.write("Hello World2\n")
    process.stdin.write("Hello World3\n")

    // process.stdout.on('message', (data) => {
    //     console.log(`stdout: ${data}`);
    // })
    process.on('spawn', (data) => {
        console.log(`stdout: ${data}`);
    })
    // console.log(process.status)
})()
