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
    
    // Ensure cargo rebuilds if build.rs itself changes
    println!("cargo:rerun-if-changed=build.rs");
}