use crate::logic::{ApplicationSettings, TrackableModel, TrackableWithChildrenModel};

use super::*;

#[derive(Facet, Serialize, Deserialize, Clone, Debug, Default)]
#[repr(C)]
pub enum QueryResponse {
    #[default]
    None,
    Trackables(Vec<TrackableModel>),
    Clicked(u32),
    Details(TrackableWithChildrenModel),
    Settings(ApplicationSettings),
}

#[derive(Facet, Serialize, Deserialize, Clone, Debug)]
#[repr(C)]
pub enum Event {
    // Shell shared events
    Query(QueryRequest),

    // Core only events
    #[serde(skip)]
    #[facet(skip)]
    Error(String),
    #[serde(skip)]
    #[facet(skip)]
    QueryResponse(QueryResponse),
}
