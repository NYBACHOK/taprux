use super::*;

#[effect(facet_typegen)]
#[derive(Debug)]
pub enum Effect {
    Render(RenderOperation),
    Query(QueryRequest),
}

#[derive(Facet, Serialize, Deserialize, Clone, Debug)]
#[repr(C)]
pub enum QueryResult {
    Response(QueryResponse),
    Err(String),
}

impl crux_core::capability::Operation for QueryRequest {
    type Output = QueryResult;
}

#[derive(Facet, Serialize, Deserialize, Clone, Debug)]
#[repr(C)]
pub enum QueryRequest {
    List,
    Clicked(u32),
    Details(u32),
}
