use colored::Colorize;
use std::env;
use std::io::stdin;
use std::str::FromStr;

fn main() {
    let args = env::args().collect::<Vec<String>>();
    if args.len() == 1 {
        print!("Hello, Kommand!");
        return;
    }
    let cmd = &args[1];
    match cmd {
        cmd if cmd == "echo" => echo(),
        cmd if cmd == "stdout" => stdout(),
        cmd if cmd == "stderr" => stderr(),
        cmd if cmd == "color" => color(),
        cmd if cmd == "interval" => interval(
            args.get(2)
                .map(|s| u32::from_str(s).unwrap_or_else(|_| panic!("{} cannot convert to u32", s)))
                .unwrap_or(5),
        ),
        _ => {
            eprintln!("Unknown command: {}", cmd);
        }
    };
}

fn echo() {
    let mut line = String::new();
    if stdin().read_line(&mut line).is_ok() {
        print!("{}", line);
    }
}

fn stdout() {
    for line in stdin().lines() {
        match line {
            Ok(line) => println!("{}", line),
            Err(_) => {
                eprint!("Error reading stdin");
                break;
            }
        }
    }
}

fn stderr() {
    for line in stdin().lines() {
        match line {
            Ok(line) => eprint!("{}", line),
            Err(_) => break,
        }
    }
}

fn interval(count: u32) {
    let duration = std::time::Duration::from_millis(100);
    for i in 0..count {
        println!("{}", i);
        std::thread::sleep(duration);
    }
}

fn color() {
    colored::control::set_override(true);
    println!("{}", "Hello, Kommand!".red());
    println!("{}", "Hello, Kommand!".green());
    println!("{}", "Hello, Kommand!".blue());
    colored::control::unset_override();
}
