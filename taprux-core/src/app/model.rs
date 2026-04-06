use crate::app::QueryResponse;

#[derive(Debug, Clone, Default)]
pub struct Model {
    pub query: QueryResponse,
    pub error: Option<String>,
}
