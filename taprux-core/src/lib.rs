use std::{path::PathBuf, sync::LazyLock};

use crux_core::{
    App, Command,
    macros::effect,
    render::{RenderOperation, render},
};
use facet::Facet;
use serde::{Deserialize, Serialize};

pub use crux_core::Core;

pub mod ffi;

mod app_state;
mod database;
mod logic;
mod setup;

static APP_DATA_DIR: LazyLock<PathBuf> = LazyLock::new(|| {
    const BUNDLE_ID: &str = "com.track.events.application";

    dirs::data_dir()
        .unwrap_or_else(|| {
            let dir = std::env::current_dir().unwrap_or_default();

            tracing::error!(data_dir = %dir.display(), "failed to get data dir will use current dir");

            dir
        })
        .join(BUNDLE_ID)
});

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

#[derive(Default)]
pub struct Model {
    count: isize,
}

#[derive(Facet, Serialize, Deserialize, Clone, Default)]
pub struct ViewModel {
    pub count: String,
}

#[derive(Default)]
pub struct Counter;

// ANCHOR: impl_app
impl App for Counter {
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

        render()
    }

    fn view(&self, model: &Model) -> ViewModel {
        ViewModel {
            count: format!("Count is: {}", model.count),
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
