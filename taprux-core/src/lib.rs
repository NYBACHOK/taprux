use std::{path::PathBuf, sync::LazyLock};

use crux_core::{
    App, Command,
    macros::effect,
    render::{RenderOperation, render},
};
use facet::Facet;
use serde::{Deserialize, Serialize};

pub use crux_core::Core;
use tokio::runtime::Runtime;

pub mod ffi;

mod database;

mod logic;
mod model;
mod setup;

#[inline]
fn data_dir() -> PathBuf {
    #[cfg(target_os = "android")]
    {
        PathBuf::from("/data/data")
    }
    #[cfg(not(target_os = "android"))]
    {
        std::env::current_dir().unwrap_or_default()
    }
}

static APP_DATA_DIR: LazyLock<PathBuf> = LazyLock::new(|| {
    const BUNDLE_ID: &str = "com.ghuba.taprux";

    dirs::data_dir()
        .unwrap_or_else(|| {
            let dir = data_dir();

            tracing::error!(data_dir = %dir.display(), "failed to get data dir will use current dir");

            dir
        })
        .join(BUNDLE_ID)
});

static TOKIO_RUNTIME: LazyLock<Runtime> =
    LazyLock::new(|| tokio::runtime::Runtime::new().expect("failed to init runtime"));

#[derive(Facet, Serialize, Deserialize, Clone, Debug)]
#[repr(C)]
pub enum Event {
    Increment,
    Decrement,
    Reset,
}

#[effect(facet_typegen)]
#[derive(Debug)]
pub enum Effect {
    Render(RenderOperation),
}

pub struct Model {
    count: isize,
    pool: sqlx::SqlitePool,
}

#[derive(Facet, Serialize, Deserialize, Clone)]
pub struct ErrorModel {
    pub is_critical: bool,
    pub description: String,
}

#[derive(Facet, Serialize, Deserialize, Clone, Default)]
pub struct ViewModel {
    pub error: Option<ErrorModel>,
    pub count: String,
}

#[derive(Default)]
pub struct Application;

impl App for Application {
    type Event = Event;
    type Model = Model;
    type ViewModel = ViewModel;
    type Effect = Effect;

    fn update(&self, event: Event, model: &mut Model) -> Command<Effect, Event> {
        match event {
            Event::Increment => model.count += 1,
            Event::Decrement => model.count -= 1,
            Event::Reset => model.count = 0,
        }

        // let command = Command::new(|ctx: CommandContext<Effect, Event>| async move {
        //     let handler = ctx.spawn(|_| async move {});

        //     let res = handler.await;
        // });

        render()
    }

    fn view(&self, model: &Model) -> ViewModel {
        ViewModel {
            count: format!("Count is: {}", model.count),
            error: None,
        }
    }
}

#[cfg(feature = "uniffi")]
const _: () = assert!(
    uniffi::check_compatible_version("0.29.4"),
    "please use uniffi v0.29.4"
);
#[cfg(feature = "uniffi")]
uniffi::setup_scaffolding!();
