use std::ffi::c_char;

use kommand_core::child::{drop_child, stdin_child, stdout_child, wait_child};
use kommand_core::ffi_util::{as_cstring, into_string};
use kommand_core::io::{drop_stdin, drop_stdout, read_line_stdout, write_line_stdin};
use kommand_core::kommand::{
    arg_command, drop_command, new_command, spawn_command, stdin_command, stdout_command,
};
use kommand_core::stdio::Stdio;

fn main() {
    unsafe {
        (0..10000).for_each(|i| {
            let command = new_command(
                as_cstring("/Users/bppleman/kgit2/kommand/eko/target/release/eko").as_ptr(),
            );
            arg_command(command, as_cstring("echo").as_ptr());
            stdin_command(command, Stdio::Pipe);
            stdout_command(command, Stdio::Pipe);
            let result = spawn_command(command);
            let child = if !result.ok.is_null() {
                result.ok
            } else {
                drop_command(command);
                panic!("{}", into_string(result.err))
            };

            let stdin = stdin_child(child);
            if stdin.is_null() {
                drop_child(child);
                drop_command(command);
                panic!("stdin is null");
            }
            let result = write_line_stdin(stdin, as_cstring("Hello, Kommand!").as_ptr());
            if result.ok != 0 {
                drop_child(child);
                drop_command(command);
                panic!("{}", into_string(result.err))
            }
            drop_stdin(stdin);

            let stdout = stdout_child(child);
            if stdout.is_null() {
                panic!("stdout is null");
            }
            let result = read_line_stdout(stdout);
            let line = if !result.ok.is_null() {
                into_string(result.ok as *mut c_char)
            } else {
                panic!("{}", into_string(result.err))
            };
            println!("[{i}] line {}", line);

            drop_stdout(stdout);
            wait_child(child);
            drop_child(child);
            drop_command(command);
            // std::thread::sleep(std::time::Duration::from_secs(1));
        });
    }
}
