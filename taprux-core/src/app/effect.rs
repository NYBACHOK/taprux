use crate::logic::ApplicationSettings;

use super::*;

#[effect(facet_typegen)]
#[derive(Debug)]
pub enum Effect {
    Render(RenderOperation),
    Changes(AppliedChanges),
    Query(QueryRequest),
}

#[derive(Facet, Serialize, Deserialize, Clone, Debug)]
#[repr(C)]
pub enum AppliedChanges {
    UserTrackable,
}

impl crux_core::capability::Operation for AppliedChanges {
    type Output = ();
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
    AllTrackables,
    UserTrackables,
    AddUserTrackable(u32),
    AddOccurrence(u32),
    DeleteOccurrence(u32),
    Occurrences,
    Details(u32),
    Settings,
    UpdateSettings(ApplicationSettings),
}
