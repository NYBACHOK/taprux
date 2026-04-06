use std::sync::Arc;

use crux_core::{
    Core,
    bridge::{BincodeFfiFormat, EffectId},
    middleware::{Bridge, HandleEffectLayer, Layer},
};

use crate::app::{Application, QueryMiddleware};

/// For the Shell to provide
#[cfg_attr(feature = "uniffi", uniffi::export(with_foreign))]
pub trait CruxShell: Send + Sync {
    /// Called when any effects resulting from an asynchronous process
    /// need processing by the shell.
    ///
    /// The bytes are a serialized vector of requests
    fn process_effects(&self, bytes: Vec<u8>);
}

/// The main interface used by the shell
#[cfg_attr(feature = "uniffi", derive(uniffi::Object))]
pub struct CoreFFI {
    core: Bridge<HandleEffectLayer<Core<Application>, QueryMiddleware>, BincodeFfiFormat>,
}

#[cfg_attr(feature = "uniffi", uniffi::export)]
impl CoreFFI {
    #[cfg_attr(feature = "uniffi", uniffi::constructor)]
    #[must_use]
    pub fn new(shell: Arc<dyn CruxShell>) -> Self {
        crate::setup::setup_logger();

        let core = Core::<Application>::new()
            .handle_effects_using(QueryMiddleware)
            .bridge::<BincodeFfiFormat>(move |effect_bytes| match effect_bytes {
                Ok(effect) => shell.process_effects(effect),
                Err(e) => panic!("{e}"),
            });

        Self { core }
    }

    #[must_use]
    pub fn update(&self, data: &[u8]) -> Vec<u8> {
        let mut effects = Vec::new();
        match self.core.update(data, &mut effects) {
            Ok(()) => effects,
            Err(e) => panic!("{e}"),
        }
    }

    #[must_use]
    pub fn resolve(&self, id: u32, data: &[u8]) -> Vec<u8> {
        let mut effects = Vec::new();
        match self.core.resolve(EffectId(id), data, &mut effects) {
            Ok(()) => effects,
            Err(e) => panic!("{e}"),
        }
    }

    #[must_use]
    pub fn view(&self) -> Vec<u8> {
        let mut view_model = Vec::new();

        match self.core.view(&mut view_model) {
            Ok(()) => view_model,
            Err(e) => panic!("{e}"),
        }
    }
}
