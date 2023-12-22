#[repr(C)]
pub enum Stdio {
    Inherit,
    Null,
    Pipe,
}
