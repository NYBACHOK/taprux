use super::*;

#[derive(Facet, Serialize, Deserialize, Clone, Debug, Default)]
#[repr(C)]
pub enum QueryResponse {
    #[default]
    None,
    List,
}

#[derive(Facet, Serialize, Deserialize, Clone, Debug)]
#[repr(C)]
pub enum Event {
    // Shell shared events
    QueryRequest(QueryRequest),
    QueryResponse(QueryResponse),

    // Core only events
    #[serde(skip)]
    #[facet(skip)]
    Error(String),
}
