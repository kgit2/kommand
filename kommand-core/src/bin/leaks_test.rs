use std::ffi::c_char;

use kommand_core::ffi_util::{as_cstring, into_string};
use kommand_core::io::{drop_stdout, read_line_stdout};
use kommand_core::process::{
    arg_command, drop_command, new_command, spawn_command, stdout_command,
};
use kommand_core::process::{buffered_stdout_child, Stdio};
use kommand_core::process::{drop_child, wait_child};

fn main() {
    unsafe {
        (0..1).for_each(|i| {
            let command = new_command(as_cstring("target/debug/kommand-echo").as_ptr());
            arg_command(command, as_cstring("echo").as_ptr());
            stdout_command(command, Stdio::Pipe);
            let result = spawn_command(command);
            let child = if !result.ok.is_null() {
                result.ok
            } else {
                drop_command(command);
                panic!("{}", into_string(result.err))
            };

            let stdout = buffered_stdout_child(child);
            if stdout.is_null() {
                panic!("stdout is null");
            }
            loop {
                let result = read_line_stdout(stdout);
                let line = if !result.ok.is_null() {
                    into_string(result.ok as *mut c_char)
                } else {
                    panic!("{}", into_string(result.err))
                };
                println!("[{i}] line {}", line);
                if line.is_empty() {
                    break;
                }
            }

            wait_child(child);
            drop_stdout(stdout);
            drop_child(child);
            drop_command(command);
        });
    }
}
