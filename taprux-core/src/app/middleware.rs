// use std::sync::OnceLock;

// use crux_core::{
//     capability::Operation,
//     middleware::{EffectMiddleware, EffectResolver},
// };

// use crate::{TOKIO_RUNTIME, setup::pre_start_setup};

// use super::*;

// #[derive(Debug, Clone, Copy, Default)]
// pub struct StateMiddleware;

// impl EffectMiddleware for StateMiddleware {
//     type Op = StateRequest;

//     fn try_process_effect(
//         &self,
//         _: Self::Op,
//         mut resolver: EffectResolver<<Self::Op as Operation>::Output>,
//     ) {
//         static STATE: OnceLock<ApplicationState> = OnceLock::new();

//         match STATE.get() {
//             Some(state) => {
//                 TOKIO_RUNTIME.spawn(async move {
//                     resolver.resolve(state.clone());
//                 });
//             }
//             None => {
//                 TOKIO_RUNTIME.spawn(async move {
//                     match pre_start_setup().await {
//                         Ok(db_pool) => resolver
//                             .resolve(STATE.get_or_init(|| ApplicationState::new(db_pool)).clone()),
//                         Err(e) => tracing::error!(error = %e,"failed to initialize database"  ),
//                     }
//                 });
//             }
//         };
//     }
// }
