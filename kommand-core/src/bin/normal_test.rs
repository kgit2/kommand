use std::io::{Read, Write};
use std::process::{Command, Stdio};

fn main() {
    (0..1).for_each(|i| {
        let mut command = Command::new("/Users/bppleman/kgit2/kommand/eko/target/release/eko");
        command
            .arg("echo")
            .stdin(Stdio::piped())
            .stdout(Stdio::piped());
        let mut child = command.spawn().unwrap();

        {
            let mut stdin = child.stdin.take().unwrap();
            stdin.write_all(b"Hello, Kommand!").unwrap();
        }

        let mut stdout = child.stdout.take().unwrap();
        let mut line = String::new();
        stdout.read_to_string(&mut line).unwrap();

        println!("[{i}] line {}", line);
    });
}
