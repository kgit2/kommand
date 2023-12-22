use std::ffi::{c_char, c_void};
use std::process::Command;

use crate::ffi_util::{as_string, into_cstring, into_void};
use crate::output::Output;
use crate::result::{IntResult, VoidResult};
use crate::stdio::Stdio;

pub fn as_command(command_ptr: &*const c_void) -> &Command {
    unsafe { &*(*command_ptr as *const Command) }
}

pub fn as_command_mut(command_ptr: &mut *const c_void) -> &mut Command {
    unsafe { &mut *(*command_ptr as *mut Command) }
}

pub fn into_command(command_ptr: *mut c_void) -> Command {
    unsafe { *Box::from_raw(command_ptr as *mut Command) }
}

/// # Safety
/// Will not move the [name]'s ownership
/// You must drop the command with [drop_command]
///
/// ```rust
/// use kommand_core::ffi_util::as_cstring;
/// use kommand_core::kommand::{drop_command, new_command};
/// unsafe {
///     let command = new_command(as_cstring("pwd").as_ptr());
///     drop_command(command);
/// }
/// ```
#[no_mangle]
pub unsafe extern "C" fn new_command(name: *const c_char) -> *mut c_void {
    let name = as_string(name);
    let command = Command::new(name);
    into_void(command)
}

/// ```rust
/// use kommand_core::ffi_util::{as_cstring, drop_string};
/// use kommand_core::kommand::{display_command, drop_command, new_command};
/// unsafe {
///     let command = new_command(as_cstring("pwd").as_ptr());
///     let display = display_command(command);
///     drop_string(display);
///     drop_command(command);
/// }
/// ```
#[no_mangle]
pub extern "C" fn display_command(command: *const c_void) -> *mut c_char {
    let command = as_command(&command);
    into_cstring(format!("{:?}", command))
}

/// ```rust
/// use kommand_core::ffi_util::{as_cstring, drop_string};
/// use kommand_core::kommand::{debug_command, drop_command, new_command};
/// unsafe {
///     let command = new_command(as_cstring("pwd").as_ptr());
///     let debug = debug_command(command);
///     drop_string(debug);
///     drop_command(command);
/// }
/// ```
#[no_mangle]
pub extern "C" fn debug_command(command: *const c_void) -> *mut c_char {
    let command = as_command(&command);
    into_cstring(format!("{:#?}", command))
}

/// ```rust
/// use kommand_core::ffi_util::as_cstring;
/// use kommand_core::kommand::{drop_command, new_command};
/// unsafe {
///     let command = new_command(as_cstring("pwd").as_ptr());
///     drop_command(command);
/// }
/// ```
#[no_mangle]
pub extern "C" fn drop_command(command: *mut c_void) {
    into_command(command);
}

/// # Safety
/// Will not take over ownership of arg
///
/// ```rust
/// use kommand_core::ffi_util::as_cstring;
/// use kommand_core::kommand::{arg_command, drop_command, new_command};
/// unsafe {
///     let command = new_command(as_cstring("ls").as_ptr());
///     arg_command(command, as_cstring("-l").as_ptr());
///     drop_command(command);
/// }
/// ```
#[no_mangle]
pub unsafe extern "C" fn arg_command(mut command: *const c_void, arg: *const c_char) {
    let command = as_command_mut(&mut command);
    let arg = as_string(arg);
    command.arg(arg);
}

/// # Safety
/// Will not take over ownership of key & value
///
/// ```rust
/// use kommand_core::ffi_util::as_cstring;
/// use kommand_core::kommand::{arg_command, drop_command, env_command, new_command};
/// unsafe {
///     let command = new_command(as_cstring("echo").as_ptr());
///     arg_command(command, as_cstring("$KOMMAND").as_ptr());
///     env_command(command, as_cstring("KOMMAND").as_ptr(), as_cstring("kommand").as_ptr());
///     drop_command(command);
/// }
/// ```
#[no_mangle]
pub unsafe extern "C" fn env_command(
    mut command: *const c_void,
    key: *const c_char,
    value: *const c_char,
) {
    let command = as_command_mut(&mut command);
    let key = as_string(key);
    let value = as_string(value);
    command.env(key, value);
}

/// # Safety
/// Will not drop the key
#[no_mangle]
pub unsafe extern "C" fn remove_env_command(mut command: *const c_void, key: *const c_char) {
    let command = as_command_mut(&mut command);
    let key = as_string(key);
    command.env_remove(key);
}

#[no_mangle]
pub extern "C" fn env_clear_command(mut command: *const c_void) {
    let command = as_command_mut(&mut command);
    command.env_clear();
}

/// # Safety
/// Will not drop the path
#[no_mangle]
pub unsafe extern "C" fn current_dir_command(mut command: *const c_void, path: *const c_char) {
    let command = as_command_mut(&mut command);
    let path = as_string(path);
    command.current_dir(path);
}

#[no_mangle]
pub extern "C" fn stdin_command(mut command: *const c_void, stdio: Stdio) {
    let command = as_command_mut(&mut command);
    match stdio {
        Stdio::Inherit => command.stdin(std::process::Stdio::inherit()),
        Stdio::Null => command.stdin(std::process::Stdio::null()),
        Stdio::Pipe => command.stdin(std::process::Stdio::piped()),
    };
}

#[no_mangle]
pub extern "C" fn stdout_command(mut command: *const c_void, stdio: Stdio) {
    let command = as_command_mut(&mut command);
    match stdio {
        Stdio::Inherit => command.stdout(std::process::Stdio::inherit()),
        Stdio::Null => command.stdout(std::process::Stdio::null()),
        Stdio::Pipe => command.stdout(std::process::Stdio::piped()),
    };
}

#[no_mangle]
pub extern "C" fn stderr_command(mut command: *const c_void, stdio: Stdio) {
    let command = as_command_mut(&mut command);
    match stdio {
        Stdio::Inherit => command.stderr(std::process::Stdio::inherit()),
        Stdio::Null => command.stderr(std::process::Stdio::null()),
        Stdio::Pipe => command.stderr(std::process::Stdio::piped()),
    };
}

#[no_mangle]
pub extern "C" fn spawn_command(mut command: *const c_void) -> VoidResult {
    let command = as_command_mut(&mut command);
    command.spawn().into()
}

#[no_mangle]
pub extern "C" fn output_command(mut command: *const c_void) -> VoidResult {
    let command = as_command_mut(&mut command);
    command.output().map(Output::from).into()
}

#[no_mangle]
pub extern "C" fn status_command(mut command: *const c_void) -> IntResult {
    let command = as_command_mut(&mut command);
    command.status().into()
}

/// [Command::get_program] returns a [OsString] which is not compatible with C.
/// unix-like: OsString is vec<u8>
/// windows: OsString is vec<u16>
#[no_mangle]
pub extern "C" fn get_program_command(command: *const c_void) -> *mut c_char {
    let command = as_command(&command);
    into_cstring(command.get_program().to_string_lossy())
}
