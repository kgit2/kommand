fn main() {
    use std::env;
    println!("PROFILE {:?}", env::var("PROFILE"));
    println!(
        "CARGO_CFG_TARGET_ARCH {:?}",
        env::var("CARGO_CFG_TARGET_ARCH")
    );
    let crate_dir = env::var("CARGO_MANIFEST_DIR").unwrap();

    cbindgen::Builder::new()
        .with_crate(crate_dir)
        .with_no_includes()
        .with_language(cbindgen::Language::C)
        .generate()
        .expect("Unable to generate bindings")
        .write_to_file("kommand_core.h");
}
