use std::sync::OnceLock;

use crux_core::{
    capability::Operation,
    middleware::{EffectMiddleware, EffectResolver},
};

use crate::{TOKIO_RUNTIME, setup::pre_start_setup};

use super::*;

#[derive(Debug, Clone, Copy, Default)]
pub struct QueryMiddleware;

impl EffectMiddleware for QueryMiddleware {
    type Op = QueryRequest;

    fn try_process_effect(
        &self,
        op: Self::Op,
        mut resolver: EffectResolver<<Self::Op as Operation>::Output>,
    ) {
        static STATE: OnceLock<ApplicationState> = OnceLock::new();

        TOKIO_RUNTIME.spawn(async move {
            let result = match STATE.get() {
                Some(state) => execute_query(state, op).await.map_err(|e| e.to_string()),
                None => match pre_start_setup().await {
                    Ok(db_pool) => {
                        let state = STATE.get_or_init(|| ApplicationState::new(db_pool)).clone();
                        execute_query(&state, op).await.map_err(|e| e.to_string())
                    }
                    Err(e) => {
                        tracing::error!(error = %e, "failed to initialize database");
                        Err(format!("Database initialization failed: {}", e))
                    }
                },
            };

            resolver.resolve(result);
        });
    }
}

async fn execute_query(
    _state: &ApplicationState,
    _op: QueryRequest,
) -> Result<QueryResponse, anyhow::Error> {
    todo!()
}
