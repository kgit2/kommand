#[repr(C)]
#[derive(Debug)]
pub enum Stdio {
    Inherit,
    Null,
    Pipe,
}
