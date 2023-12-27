use crate::ffi_util::{into_cstring, into_void};
use std::ffi::{c_char, c_int, c_void};
use std::fmt::Display;
use std::io;

#[repr(C)]
pub struct VoidResult {
    pub ok: *mut c_void,
    pub err: *mut c_char,
    pub error_type: ErrorType,
}

#[repr(C)]
pub struct IntResult {
    pub ok: c_int,
    pub err: *mut c_char,
    pub error_type: ErrorType,
}

#[repr(C)]
pub struct UnitResult {
    pub ok: c_int,
    pub err: *mut c_char,
    pub error_type: ErrorType,
}

#[repr(C)]
pub enum ErrorType {
    None,
    Io,
    Utf8,
    Unknown,
}

impl VoidResult {
    pub fn error(err: impl AsRef<str>, error_type: ErrorType) -> Self {
        VoidResult {
            ok: std::ptr::null_mut(),
            err: into_cstring(err.as_ref()),
            error_type,
        }
    }
}

impl From<io::Result<std::process::Child>> for VoidResult {
    //noinspection DuplicatedCode
    fn from(result: io::Result<std::process::Child>) -> Self {
        match result {
            Ok(child) => VoidResult {
                ok: into_void(child),
                err: std::ptr::null_mut() as *mut c_char,
                error_type: ErrorType::None,
            },
            Err(e) => VoidResult {
                ok: std::ptr::null_mut(),
                err: into_cstring(e.to_string()),
                error_type: ErrorType::Io,
            },
        }
    }
}

impl From<io::Result<crate::process::Output>> for VoidResult {
    //noinspection DuplicatedCode
    fn from(value: io::Result<crate::process::Output>) -> Self {
        match value {
            Ok(output) => VoidResult {
                ok: into_void(output),
                err: std::ptr::null_mut() as *mut c_char,
                error_type: ErrorType::None,
            },
            Err(e) => VoidResult {
                ok: std::ptr::null_mut(),
                err: into_cstring(e.to_string()),
                error_type: ErrorType::Io,
            },
        }
    }
}

impl<E: Display> From<Result<String, E>> for VoidResult {
    fn from(value: Result<String, E>) -> Self {
        match value {
            Ok(string) => VoidResult {
                ok: into_cstring(string) as *mut c_void,
                err: std::ptr::null_mut() as *mut c_char,
                error_type: ErrorType::None,
            },
            Err(e) => VoidResult {
                ok: std::ptr::null_mut(),
                err: into_cstring(e.to_string()),
                error_type: ErrorType::Io,
            },
        }
    }
}

impl From<io::Result<std::process::ExitStatus>> for IntResult {
    fn from(value: io::Result<std::process::ExitStatus>) -> Self {
        match value {
            Ok(status) => match status.code() {
                None => IntResult {
                    ok: -1,
                    err: into_cstring("No exit code"),
                    error_type: ErrorType::None,
                },
                Some(code) => IntResult {
                    ok: code,
                    err: std::ptr::null_mut() as *mut c_char,
                    error_type: ErrorType::None,
                },
            },
            Err(e) => IntResult {
                ok: -1,
                err: into_cstring(e.to_string()),
                error_type: ErrorType::Io,
            },
        }
    }
}

impl From<io::Result<()>> for UnitResult {
    fn from(value: io::Result<()>) -> Self {
        match value {
            Ok(_) => UnitResult {
                ok: 0,
                err: std::ptr::null_mut() as *mut c_char,
                error_type: ErrorType::None,
            },
            Err(e) => UnitResult {
                ok: -1,
                err: into_cstring(e.to_string()),
                error_type: ErrorType::Io,
            },
        }
    }
}
