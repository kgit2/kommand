use std::io::{stdout, Write};
use std::process::{Command, Stdio};

fn main() {
    println!("{:?}", std::env::current_dir());
    (0..1).for_each(|_| {
        let mut command = Command::new("target/debug/kommand-echo");
        command.arg("interval").stdout(Stdio::inherit());
        let child = command.spawn().unwrap();
        let output = child.wait_with_output().unwrap();
        println!("output");
        stdout().write_all(&output.stdout).unwrap();
    });
}
