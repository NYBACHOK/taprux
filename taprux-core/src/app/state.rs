use std::{ops::Deref, sync::Arc};

#[derive(Debug, Clone)]
pub struct ApplicationState(Arc<InnerApplicationState>);

#[derive(Debug)]
pub struct InnerApplicationState {
    pub db_pool: sqlx::SqlitePool,
}

impl ApplicationState {
    pub fn new(db_pool: sqlx::SqlitePool) -> Self {
        Self(Arc::new(InnerApplicationState { db_pool }))
    }
}

impl Deref for ApplicationState {
    type Target = InnerApplicationState;

    fn deref(&self) -> &Self::Target {
        &self.0
    }
}
