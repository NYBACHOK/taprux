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
                Err(e) => {
                    tracing::error!(error = %e, "failed to read from database database ");
                    QueryResult::Err(e.to_string())
                }
            });
        });
    }
}

async fn execute_query(
    state: &ApplicationState,
    op: QueryRequest,
) -> Result<QueryResponse, anyhow::Error> {
    tracing::info!("processing query: {op:#?}");

    let response = match op {
        QueryRequest::AllTrackables => QueryResponse::Trackables(
            logic::list(&state.db_pool, 0)
                .await
                .context("retrieving list of trackables")?,
        ),
        QueryRequest::UserTrackables => QueryResponse::Trackables(
            logic::user_list(&state.db_pool)
                .await
                .context("retrieving list of user trackables")?,
        ),
        QueryRequest::AddOccurrence(id) => {
            logic::clicked(&state.db_pool, id)
                .await
                .context("adding new trackable occurence")?;

            QueryResponse::Clicked(id)
        }
        QueryRequest::DeleteOccurrence(id) => {
            logic::occurrence_delete(&state.db_pool, id)
                .await
                .context("deletion of trackable occurrence")?;

            QueryResponse::DeletedOccurrence(id)
        }

        QueryRequest::Occurrences => {
            let occurrences = logic::occurrences(&state.db_pool)
                .await
                .context("retrieval of trackable occurrence")?;

            QueryResponse::Occurrences(occurrences)
        }
        QueryRequest::AddUserTrackable(id) => {
            logic::user_trackables_add(&state.db_pool, id).await?;

            QueryResponse::AddedUserTrackable
        }
        QueryRequest::Details(id) => QueryResponse::Details(
            logic::details(&state.db_pool, id)
                .await
                .context("getting details of multi trackable")?,
        ),
        QueryRequest::Settings => QueryResponse::Settings(
            logic::application_settings(&state.db_pool)
                .await
                .context("retrieving application settings")?,
        ),
        QueryRequest::UpdateSettings(settings) => QueryResponse::Settings(
            logic::update_application_settings(&state.db_pool, settings)
                .await
                .context("updating application settings")?,
        ),
    };

    Ok(response)
}
