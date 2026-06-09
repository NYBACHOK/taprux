use std::sync::Arc;

use crux_core::{
    Core,
    bridge::{BincodeFfiFormat, EffectId},
    effects::{
        EffectRouter, Routes,
        routes::{Buffer, Serialized},
    },
};

use crate::{Application, QueryRequest};

#[boltffi::export]
pub trait CruxShell: Send + Sync {
    /// Called when any effects resulting from an asynchronous process
    /// need processing by the shell.
    ///
    /// The bytes are a serialized vector of requests.
    fn process_effects(&self, bytes: Vec<u8>);
}

#[derive(Clone)]
pub struct EffectRoutes {
    pub(crate) serialized: Arc<Serialized<Application, Self, BincodeFfiFormat>>,
    pub(crate) query: Arc<Buffer<QueryRequest>>,
}

impl Routes<Application> for EffectRoutes {
    fn new(router: std::sync::Weak<crux_core::effects::EffectRouter<Application, Self>>) -> Self {
        Self {
            serialized: Arc::new(Serialized::new(router.clone())),
            query: Arc::new(Buffer::default()),
        }
    }
}

/// The main interface used by the shell
/// 
pub struct CoreFFI {
    router: Arc<EffectRouter<Application, EffectRoutes>>,
}

#[boltffi::export]
#[allow(clippy::missing_panics_doc)]
impl CoreFFI {
    pub fn new(shell: Arc<dyn CruxShell>) -> Self {
        crate::setup::setup_logger();

        let router = EffectRouter::new(Core::new(), move |routes: EffectRoutes| {
            let shell = shell.clone();

            move |effect| match effect {
                crate::Effect::Query(req) => {
                    routes.query.push(req);
                }
                effect => {
                    let bytes = routes
                        .serialized
                        .serialize(effect)
                        .expect("serialized effect request should encode");

                    shell.process_effects(bytes);
                }
            }
        });

        Self { router }
    }

    #[must_use]
    pub fn update(&self, data: &[u8]) -> Vec<u8> {
        match self.router.routes.serialized.update(data) {
            Ok(()) => Vec::new(),
            Err(e) => panic!("{e}"),
        }
    }

    #[must_use]
    pub fn resolve(&self, effect_id: u32, data: &[u8]) -> Vec<u8> {
        self.router
            .routes
            .serialized
            .resolve(EffectId(effect_id), data)
            .expect("failed to resolve effect");

        Vec::new()
    }

    #[must_use]
    pub fn view(&self) -> Vec<u8> {
        self.router
            .routes
            .serialized
            .view()
            .expect("view model should serialize")
    }
}
