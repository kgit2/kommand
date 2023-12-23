use std::ffi::{c_char, c_void, CStr, CString};

/// # Safety
pub unsafe fn as_string(ptr: *const c_char) -> String {
    assert!(!ptr.is_null(), "char ptr is null");
    CStr::from_ptr(ptr)
        .to_str()
        .expect("convert char ptr to CStr failed")
        .to_string()
}

/// # Safety
pub unsafe fn into_string(ptr: *mut c_char) -> String {
    assert!(!ptr.is_null(), "char ptr is null");
    CString::from_raw(ptr)
        .into_string()
        .expect("convert char ptr to CString failed")
}

pub fn as_cstring(str: impl AsRef<str>) -> CString {
    CString::new(str.as_ref()).expect("convert to CString failed")
}

pub fn into_cstring(str: impl AsRef<str>) -> *mut c_char {
    CString::new(str.as_ref())
        .expect("convert to CString failed")
        .into_raw()
}

/// # Safety
/// Will drop the string
#[no_mangle]
pub unsafe extern "C" fn drop_string(string: *mut c_char) {
    if string.is_null() {
        return;
    }
    into_string(string);
}

pub fn into_void<T>(object: T) -> *mut c_void {
    Box::into_raw(Box::new(object)) as *mut c_void
}

#[no_mangle]
pub extern "C" fn void_to_string(ptr: *mut c_void) -> *mut c_char {
    if ptr.is_null() {
        return std::ptr::null_mut();
    }
    ptr as *mut c_char
}
