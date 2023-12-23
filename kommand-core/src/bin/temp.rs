use std::process::Command;

fn main() {
    println!("{:?}", std::env::current_dir());
    let output = Command::new("target/debug/kommand-echo")
        .arg("color")
        .output()
        .unwrap();
    println!("{:?}", output);
}
