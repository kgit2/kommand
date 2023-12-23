use std::ffi::c_void;
use std::io::BufWriter;
use std::process::ChildStdin;

pub fn as_stdin_mut(reader: &mut *const c_void) -> &mut BufWriter<ChildStdin> {
    unsafe { &mut *(*reader as *mut BufWriter<ChildStdin>) }
}

pub fn into_stdin(reader: *mut c_void) -> BufWriter<ChildStdin> {
    unsafe { *Box::from_raw(reader as *mut BufWriter<ChildStdin>) }
}

#[no_mangle]
pub extern "C" fn drop_stdin(reader: *mut c_void) {
    if reader.is_null() {
        return;
    }
    into_stdin(reader);
}
