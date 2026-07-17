// build.rs
use std::fs;
use std::path::Path;

fn main() {
    let build_file_path = Path::new("build_number.txt");
    let mut build_num = 1;

    // Read the current build number if the file exists
    if build_file_path.exists() {
        if let Ok(content) = fs::read_to_string(build_file_path) {
            if let Ok(num) = content.trim().parse::<u32>() {
                build_num = num + 1;
            }
        }
    }

    // Save the incremented build number back to the file
    let _ = fs::write(build_file_path, build_num.to_string());

    // Pass the build number to the main Rust compiler as an environment variable
    println!("cargo:rustc-env=BUFFALO_BUILD_NUMBER={}", build_num);

    // NOTE: deliberately no `cargo:rerun-if-changed` directive here. Cargo's default with zero
    // rerun-if directives is "always rerun this build script" -- exactly what a build counter
    // needs. The moment even one rerun-if-changed is emitted, Cargo switches to ONLY rerunning
    // when that specific file changes, which is why this counter was previously stuck unless
    // build.rs itself was edited (or `cargo clean` wiped the cached "don't need to rerun" state).
}