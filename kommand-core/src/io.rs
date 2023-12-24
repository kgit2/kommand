use crate::ffi_util::as_string;
use std::ffi::{c_char, c_void};
use std::io::{BufRead, Read, Write};

use crate::io::stdout::as_stdout_mut;
use crate::result::{UnitResult, VoidResult};

mod stderr;
mod stdin;
mod stdout;

pub use stderr::drop_stderr;
pub use stdin::drop_stdin;
pub use stdout::drop_stdout;

#[no_mangle]
pub extern "C" fn read_line_stdout(mut reader: *const c_void) -> VoidResult {
    let reader = as_stdout_mut(&mut reader);
    let mut line = String::new();
    reader
        .read_line(&mut line)
        .map(|_| line.trim_end_matches('\n').to_string())
        .into()
}

#[no_mangle]
pub extern "C" fn read_all_stdout(mut reader: *const c_void) -> VoidResult {
    let reader = as_stdout_mut(&mut reader);
    let mut line = String::new();
    reader.read_to_string(&mut line).map(|_| line).into()
}

#[no_mangle]
pub extern "C" fn read_line_stderr(mut reader: *const c_void) -> VoidResult {
    let reader = stderr::as_stderr_mut(&mut reader);
    let mut line = String::new();
    reader
        .read_line(&mut line)
        .map(|_| line.trim_end_matches('\n').to_string())
        .into()
}

#[no_mangle]
pub extern "C" fn read_all_stderr(mut reader: *const c_void) -> VoidResult {
    let reader = stderr::as_stderr_mut(&mut reader);
    let mut line = String::new();
    reader.read_to_string(&mut line).map(|_| line).into()
}

/// # Safety
#[no_mangle]
pub unsafe extern "C" fn write_line_stdin(
    mut writer: *const c_void,
    line: *const c_char,
) -> UnitResult {
    let writer = stdin::as_stdin_mut(&mut writer);
    let line = as_string(line);
    let result = writer
        .write_all(line.as_bytes())
        .and_then(|_| writer.write(b"\n"))
        .map(|_| ());
    result.into()
}

#[no_mangle]
pub extern "C" fn flush_stdin(mut writer: *const c_void) -> UnitResult {
    let writer = stdin::as_stdin_mut(&mut writer);
    writer.flush().into()
}
