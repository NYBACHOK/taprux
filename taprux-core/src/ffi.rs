use crux_core::{
    Core,
    bridge::{BincodeFfiFormat, Bridge, EffectId, FfiFormat},
};

use crate::{Application, ErrorModel, Model, TOKIO_RUNTIME, ViewModel};

pub enum CoreState {
    Corrupted(String),
    Normal(Bridge<Application>),
}

/// The main interface used by the shell
#[cfg_attr(feature = "uniffi", derive(uniffi::Object))]
#[cfg_attr(feature = "wasm_bindgen", wasm_bindgen::prelude::wasm_bindgen)]
pub struct CoreFFI {
    core: CoreState,
}

#[cfg_attr(feature = "uniffi", uniffi::export)]
#[cfg_attr(feature = "wasm_bindgen", wasm_bindgen::prelude::wasm_bindgen)]
impl CoreFFI {
    #[cfg_attr(feature = "uniffi", uniffi::constructor)]
    #[cfg_attr(
        feature = "wasm_bindgen",
        wasm_bindgen::prelude::wasm_bindgen(constructor)
    )]
    #[must_use]
    pub fn new() -> Self {
        fn inner_new() -> Result<Bridge<Application>, String> {
            let pool = TOKIO_RUNTIME
                .block_on(crate::setup::pre_start_setup())
                .map_err(|e| e.to_string())?;

            Ok(Bridge::new(Core::new_with(
                Application,
                Model { count: 0, pool },
            )))
        }

        match inner_new() {
            Ok(app) => Self {
                core: CoreState::Normal(app),
            },
            Err(err) => Self {
                core: CoreState::Corrupted(err),
            },
        }
    }

    /// Send an event to the app and return the effects.
    /// # Panics
    /// If the event cannot be deserialized.
    /// In production you should handle the error properly.
    #[must_use]
    pub fn update(&self, data: &[u8]) -> Vec<u8> {
        match &self.core {
            CoreState::Corrupted(e) => panic!("{e}"),
            CoreState::Normal(bridge) => {
                let mut effects = Vec::new();
                match bridge.update(data, &mut effects) {
                    Ok(()) => effects,
                    Err(e) => panic!("{e}"),
                }
            }
        }
    }

    /// Resolve an effect and return the effects.
    /// # Panics
    /// If the `data` cannot be deserialized into an effect or the `effect_id` is invalid.
    /// In production you should handle the error properly.
    #[must_use]
    pub fn resolve(&self, id: u32, data: &[u8]) -> Vec<u8> {
        match &self.core {
            CoreState::Corrupted(e) => panic!("{e}"),
            CoreState::Normal(bridge) => {
                let mut effects = Vec::new();
                match bridge.resolve(EffectId(id), data, &mut effects) {
                    Ok(()) => effects,
                    Err(e) => panic!("{e}"),
                }
            }
        }
    }

    /// Get the current `ViewModel`.
    /// # Panics
    /// If the view cannot be serialized.
    /// In production you should handle the error properly.
    #[must_use]
    pub fn view(&self) -> Vec<u8> {
        let mut view_model = Vec::new();

        match &self.core {
            CoreState::Corrupted(e) => {
                BincodeFfiFormat::serialize(
                    &mut view_model,
                    &ViewModel {
                        count: String::new(),
                        error: Some(ErrorModel {
                            is_critical: true,
                            description: e.to_string(),
                        }),
                    },
                )
                .expect("serialization valid"); // TODO: how could write fail if I write into Vec?

                view_model
            }
            CoreState::Normal(bridge) => match bridge.view(&mut view_model) {
                Ok(()) => view_model,
                Err(e) => panic!("{e}"),
            },
        }
    }
}
