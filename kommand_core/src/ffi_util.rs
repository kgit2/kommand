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

// /// # Safety
// #[no_mangle]
// pub unsafe extern "C" fn into_i32(int: *mut i32) -> i32 {
//     *Box::from_raw(int)
// }
//
// /// # Safety
// #[no_mangle]
// #[cfg(unix)]
// pub unsafe extern "C" fn u8_ptr_from_os_string(ptr: *mut c_void) -> *mut c_uchar {
//     let os_string = *Box::from_raw(ptr as *mut OsString);
//     let mut u8_vec = std::os::unix::prelude::OsStringExt::into_vec(os_string);
//     let ptr = u8_vec.as_mut_ptr();
//     std::mem::forget(u8_vec);
//     ptr
// }
//
// /// # Safety
// #[no_mangle]
// #[cfg(not(unix))]
// pub unsafe extern "C" fn u8_ptr_from_os_string(ptr: *mut c_void) -> *mut c_uchar {
//     let _ = *Box::from_raw(ptr as *mut OsString);
//     std::ptr::null_mut()
// }
//
// /// # Safety
// #[no_mangle]
// #[cfg(windows)]
// pub unsafe extern "C" fn u16_ptr_from_os_string(ptr: *mut c_void) -> *mut u16 {
//     let os_string = *Box::from_raw(ptr as *mut OsString);
//     let mut u16_vec: Vec<u16> = std::os::windows::ffi::OsStringExt::encode_wide(os_string)
//         .chain(Some(0))
//         .collect();
//     let ptr = u16_vec.as_mut_ptr();
//     std::mem::forget(u16_vec);
//     ptr
// }
//
// /// # Safety
// #[no_mangle]
// #[cfg(not(windows))]
// pub unsafe extern "C" fn u16_ptr_from_os_string(ptr: *mut c_void) -> *mut u16 {
//     let _ = *Box::from_raw(ptr as *mut OsString);
//     std::ptr::null_mut()
// }

/// # Safety
/// Will drop the string
#[no_mangle]
pub unsafe extern "C" fn drop_string(string: *mut c_char) {
    into_string(string);
}

pub fn into_void<T>(object: T) -> *mut c_void {
    Box::into_raw(Box::new(object)) as *mut c_void
}
