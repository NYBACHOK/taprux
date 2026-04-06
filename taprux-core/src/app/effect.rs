use super::*;

#[effect(facet_typegen)]
#[derive(Debug)]
pub enum Effect {
    Render(RenderOperation),
    Query(QueryRequest),
}

impl crux_core::capability::Operation for QueryRequest {
    type Output = Result<QueryResponse, String>;
}

#[derive(Facet, Serialize, Deserialize, Clone, Debug)]
#[repr(C)]
pub enum QueryRequest {
    List,
}
