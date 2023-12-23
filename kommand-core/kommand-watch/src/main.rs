use std::collections::{HashMap, HashSet};
use std::ffi::OsStr;
use std::path::Path;
use std::time::Duration;

use color_eyre::eyre::Context;
use color_eyre::{install, Result};
use lazy_static::lazy_static;
use notify_debouncer_mini::notify::RecursiveMode;
use notify_debouncer_mini::{new_debouncer, DebounceEventResult};
use std::path::PathBuf;
use walkdir::WalkDir;

lazy_static! {
    static ref ROOT_DIR: PathBuf = PathBuf::from(env!("CARGO_MANIFEST_DIR"))
        .parent()
        .unwrap()
        .parent()
        .unwrap()
        .canonicalize()
        .unwrap();
    static ref MAIN_DIR: PathBuf = ROOT_DIR.join("src/macosX64Main/kotlin/com/kgit2/wrapper");
    #[rustfmt::skip]
    static ref CLUSTER_DIRS: HashMap<String, PathBuf> = HashMap::from([
        ("macosArm64".to_string(), ROOT_DIR.join("src/macosArm64Main/kotlin/com/kgit2/wrapper")),
        ("linuxX64".to_string(), ROOT_DIR.join("src/linuxX64Main/kotlin/com/kgit2/wrapper")),
        ("linuxArm64".to_string(), ROOT_DIR.join("src/linuxArm64Main/kotlin/com/kgit2/wrapper")),
        ("mingwX64".to_string(), ROOT_DIR.join("src/mingwX64Main/kotlin/com/kgit2/wrapper")),
    ]);
}

#[tokio::main(flavor = "current_thread")]
async fn main() -> Result<()> {
    install()?;

    sync_dir(MAIN_DIR.as_path());
    let mut debouncer = new_debouncer(Duration::from_millis(500), process_file_change)?;
    println!("Watching {}", MAIN_DIR.display());
    debouncer
        .watcher()
        .watch(&MAIN_DIR, RecursiveMode::Recursive)?;

    tokio::signal::ctrl_c().await?;
    Ok(())
}

fn process_file_change(result: DebounceEventResult) {
    match result {
        Ok(result) => {
            result
                .into_iter()
                .filter(|e| e.path.extension() == Some(OsStr::new("kt")))
                .for_each(|e| {
                    let path = e.path;
                    if path.exists() {
                        if path.is_file() {
                            sync_file(path);
                        } else {
                            sync_dir(path);
                        }
                    } else {
                        sync_remove(path);
                    }
                });
        }
        Err(error) => {
            eprintln!("{:?}", error);
        }
    }
}

fn sync_file(path: impl AsRef<Path>) {
    println!("File changed {:?}", path.as_ref());
    let content = fs_extra::file::read_to_string(path.as_ref()).unwrap();
    let striped = path
        .as_ref()
        .strip_prefix(MAIN_DIR.as_path())
        .with_context(|| "Strip Error")
        .unwrap();
    CLUSTER_DIRS.iter().for_each(|(name, cluster_dir)| {
        let new_path = cluster_dir.join(striped.to_string_lossy().replace("macosX64", name));
        println!("Will write to {:?}", new_path);
        if !new_path.exists() {
            fs_extra::dir::create_all(new_path.parent().unwrap(), false).unwrap();
        }
        fs_extra::file::write_all(&new_path, &content).unwrap();
    });
}

fn sync_dir(path: impl AsRef<Path>) {
    println!("Dir changed {:?}", path.as_ref());
    let main_files = collect_dir(MAIN_DIR.as_path());
    let main_set = main_files
        .iter()
        .map(|file| {
            file.strip_prefix(MAIN_DIR.as_path())
                .unwrap()
                .to_string_lossy()
                .to_string()
        })
        .collect::<HashSet<_>>();
    CLUSTER_DIRS.iter().for_each(|(name, cluster_dir)| {
        let cluster_files = collect_dir(cluster_dir.as_path());
        let cluster_set = cluster_files
            .iter()
            .map(|file| {
                let file = file.strip_prefix(cluster_dir.as_path()).unwrap();
                if let Some(file_name) = file.file_name() {
                    file_name.to_string_lossy().replace(name, "macosX64")
                } else {
                    file.to_string_lossy().to_string()
                }
            })
            .collect::<HashSet<_>>();
        let diff = cluster_set.difference(&main_set);
        diff.for_each(|path| sync_remove(cluster_dir.join(path)));
        #[rustfmt::skip]
        main_set.iter().for_each(|path| sync_file(MAIN_DIR.join(path)));
    });
}

fn sync_remove(path: impl AsRef<Path>) {
    println!("Item removed {:?}", path.as_ref());
    fs_extra::remove_items(&[path.as_ref()]).unwrap();
}

fn collect_dir(path: impl AsRef<Path>) -> HashSet<PathBuf> {
    let walk_dir = WalkDir::new(path.as_ref()).min_depth(1);
    walk_dir
        .into_iter()
        .flatten()
        .map(|entry| entry.into_path())
        .collect::<HashSet<_>>()
}
