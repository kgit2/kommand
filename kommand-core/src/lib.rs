//! This is a [std::process::Command] wrapper for ffi
//!
//! Export the ffi interface through [cbindgen] and use it in kotlin/native

pub mod env;
pub mod ffi_util;
pub mod io;
pub mod process;
pub mod result;
