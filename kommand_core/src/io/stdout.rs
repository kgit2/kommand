use std::ffi::c_void;
use std::io::BufReader;
use std::process::ChildStdout;

pub fn as_stdout_mut(reader: &mut *const c_void) -> &mut BufReader<ChildStdout> {
    unsafe { &mut *(*reader as *mut BufReader<ChildStdout>) }
}

pub fn into_stdout(reader: *mut c_void) -> BufReader<ChildStdout> {
    unsafe { *Box::from_raw(reader as *mut BufReader<ChildStdout>) }
}

#[no_mangle]
pub extern "C" fn drop_stdout(reader: *mut c_void) {
    into_stdout(reader);
}
