use crate::ffi_util::into_void;
use crate::process::Output;
use crate::result::{ErrorType, IntResult, UnitResult, VoidResult};
use std::ffi::{c_uint, c_void};
use std::io::{BufReader, BufWriter};
use std::process::Child;

pub fn as_child(command_ptr: &*const c_void) -> &Child {
    unsafe { &*(*command_ptr as *const Child) }
}

pub fn as_child_mut(command_ptr: &mut *const c_void) -> &mut Child {
    unsafe { &mut *(*command_ptr as *mut Child) }
}

pub fn into_child(command_ptr: *mut c_void) -> Child {
    unsafe { *Box::from_raw(command_ptr as *mut Child) }
}

#[no_mangle]
pub extern "C" fn buffered_stdin_child(mut child: *const c_void) -> *mut c_void {
    let child = as_child_mut(&mut child);
    child
        .stdin
        .take()
        .map(BufWriter::new)
        .map(into_void)
        .unwrap_or(std::ptr::null_mut())
}

#[no_mangle]
pub extern "C" fn buffered_stdout_child(mut child: *const c_void) -> *mut c_void {
    let child = as_child_mut(&mut child);
    child
        .stdout
        .take()
        .map(BufReader::new)
        .map(into_void)
        .unwrap_or(std::ptr::null_mut())
}

#[no_mangle]
pub extern "C" fn buffered_stderr_child(mut child: *const c_void) -> *mut c_void {
    let child = as_child_mut(&mut child);
    child
        .stderr
        .take()
        .map(BufReader::new)
        .map(into_void)
        .unwrap_or(std::ptr::null_mut())
}

#[no_mangle]
pub extern "C" fn kill_child(mut child: *const c_void) -> UnitResult {
    let child = as_child_mut(&mut child);
    child.kill().into()
}

#[no_mangle]
pub extern "C" fn id_child(child: *const c_void) -> c_uint {
    let child = as_child(&child);
    child.id()
}

#[no_mangle]
pub extern "C" fn wait_child(mut child: *const c_void) -> IntResult {
    let child = as_child_mut(&mut child);
    child.wait().into()
}

/// The returned [Output] will empty
/// if convert the [Child.stdout] and [Child.stderr] to buffered
/// with [buffered_stdout_child] and [buffered_stderr_child]
#[no_mangle]
pub extern "C" fn wait_with_output_child(child: *mut c_void) -> VoidResult {
    if child.is_null() {
        return VoidResult::error("Child has been consumed", ErrorType::None);
    }
    let child = into_child(child);
    child.wait_with_output().map(Output::from).into()
}

#[no_mangle]
pub extern "C" fn drop_child(child: *mut c_void) {
    if child.is_null() {
        return;
    }
    into_child(child);
}
