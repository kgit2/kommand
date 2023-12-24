use std::ffi::{c_char, c_void};
use std::os::raw::c_int;

use crate::ffi_util::{into_cstring, into_string};

#[repr(C)]
pub struct Output {
    pub exit_code: c_int,
    pub stdout_content: *mut c_char,
    pub stderr_content: *mut c_char,
}

#[no_mangle]
pub extern "C" fn into_output(ptr: *mut c_void) -> Output {
    unsafe { *Box::from_raw(ptr as *mut Output) }
}

#[no_mangle]
pub extern "C" fn drop_output(output: Output) {
    let stdout = output.stdout_content;
    if !stdout.is_null() {
        let _ = unsafe { into_string(stdout) };
    }
    let stderr = output.stderr_content;
    if !stderr.is_null() {
        let _ = unsafe { into_string(stderr) };
    }
}

impl From<std::process::Output> for Output {
    fn from(value: std::process::Output) -> Self {
        let exit_code = value.status.code().unwrap_or(-1);
        let stdout = String::from_utf8_lossy(&value.stdout).to_string();
        let stderr = String::from_utf8_lossy(&value.stderr).to_string();

        Output {
            exit_code,
            stdout_content: into_cstring(stdout),
            stderr_content: into_cstring(stderr),
        }
    }
}
