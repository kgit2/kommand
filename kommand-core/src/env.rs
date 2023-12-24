use crate::ffi_util::{as_string, into_cstring, into_string};
use std::ffi::{c_char, c_ulonglong};

#[repr(C)]
pub struct EnvVars {
    pub names: *mut *mut c_char,
    pub values: *mut *mut c_char,
    pub len: c_ulonglong,
}

/// # Safety
#[no_mangle]
pub unsafe extern "C" fn env_var(name: *const c_char) -> *mut c_char {
    let name = as_string(name);
    into_cstring(std::env::var(name).unwrap_or_default())
}

#[no_mangle]
pub extern "C" fn env_vars() -> EnvVars {
    let (mut names, mut values) = std::env::vars().fold(
        (Vec::new(), Vec::new()),
        |(mut names, mut values), (name, value)| {
            names.push(into_cstring(name));
            values.push(into_cstring(value));
            (names, values)
        },
    );
    let env_vars = EnvVars {
        names: names.as_mut_ptr(),
        values: values.as_mut_ptr(),
        len: names.len() as u64,
    };
    std::mem::forget(names);
    std::mem::forget(values);
    env_vars
}

#[no_mangle]
pub extern "C" fn drop_env_vars(env_vars: EnvVars) {
    unsafe {
        let len = env_vars.len as usize;
        Vec::from_raw_parts(env_vars.names, len, len)
            .into_iter()
            .for_each(|name| {
                into_string(name);
            });
        Vec::from_raw_parts(env_vars.values, len, len)
            .into_iter()
            .for_each(|value| {
                into_string(value);
            });
    }
}
