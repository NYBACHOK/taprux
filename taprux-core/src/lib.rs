use std::{path::PathBuf, sync::LazyLock};

pub use crux_core::Core;
use tokio::runtime::Runtime;

pub mod ffi;

mod app;
mod database;
mod logic;
mod setup;

pub use app::*;

#[inline]
fn data_dir() -> PathBuf {
    #[cfg(target_os = "android")]
    {
        PathBuf::from("/data/data")
    }
    #[cfg(not(target_os = "android"))]
    {
        dirs::data_dir().unwrap_or_else(|| {
            let dir = std::env::current_dir().unwrap_or_default();

            tracing::error!(data_dir = %dir.display(), "failed to get data dir will ");

            dir
        })
    }
}

static APP_DATA_DIR: LazyLock<PathBuf> = LazyLock::new(|| {
    // TODO: for tests I need use tmp_dir
    const BUNDLE_ID: &str = "com.ghuba.taprux";

    data_dir().join(BUNDLE_ID)
});

static TOKIO_RUNTIME: LazyLock<Runtime> =
    LazyLock::new(|| tokio::runtime::Runtime::new().expect("failed to init runtime"));

#[cfg(feature = "uniffi")]
const _: () = assert!(
    uniffi::check_compatible_version("0.29.4"),
    "please use uniffi v0.29.4"
);
#[cfg(feature = "uniffi")]
uniffi::setup_scaffolding!();
