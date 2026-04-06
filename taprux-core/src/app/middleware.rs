use std::sync::OnceLock;

use anyhow::Context;
use crux_core::{
    capability::Operation,
    middleware::{EffectMiddleware, EffectResolver},
};

use crate::{TOKIO_RUNTIME, logic, setup::pre_start_setup};

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
                Some(state) => execute_query(state, op).await,
                None => match pre_start_setup().await {
                    Ok(db_pool) => {
                        let state = STATE.get_or_init(|| ApplicationState::new(db_pool)).clone();
                        execute_query(&state, op).await
                    }
                    Err(e) => {
                        tracing::error!(error = %e, "failed to initialize database");
                        Err(anyhow::anyhow!("Database initialization failed: {}", e))
                    }
                },
            };

            resolver.resolve(match result {
                Ok(query) => QueryResult::Response(query),
                Err(err) => QueryResult::Err(err.to_string()),
            });
        });
    }
}

async fn execute_query(
    state: &ApplicationState,
    op: QueryRequest,
) -> Result<QueryResponse, anyhow::Error> {
    let response = match op {
        QueryRequest::List => QueryResponse::List(
            logic::list(&state.db_pool, 0)
                .await
                .context("retrieving list of trackables")?,
        ),
        QueryRequest::Clicked(id) => {
            logic::clicked(&state.db_pool, id)
                .await
                .context("adding new trackable occurence")?;

            QueryResponse::Clicked(id)
        }
        QueryRequest::Details(id) => QueryResponse::Details(
            logic::details(&state.db_pool, id)
                .await
                .context("getting details of multi trackable")?,
        ),
    };

    Ok(response)
}
