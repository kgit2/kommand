use std::ffi::c_void;
use std::io::BufReader;
use std::process::ChildStderr;

pub fn as_stderr_mut(reader: &mut *const c_void) -> &mut BufReader<ChildStderr> {
    unsafe { &mut *(*reader as *mut BufReader<ChildStderr>) }
}

pub fn into_stderr(reader: *mut c_void) -> BufReader<ChildStderr> {
    unsafe { *Box::from_raw(reader as *mut BufReader<ChildStderr>) }
}

#[no_mangle]
pub extern "C" fn drop_stderr(reader: *mut c_void) {
    if reader.is_null() {
        return;
    }
    into_stderr(reader);
}
