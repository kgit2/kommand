//! This is a [std::process::Command] wrapper for ffi
//!
//! Export the ffi interface through [cbindgen] and use it in kotlin/native

pub mod child;
pub mod ffi_util;
pub mod io;
pub mod kommand;
pub mod output;
pub mod result;
pub mod stdio;
