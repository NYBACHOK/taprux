use std::collections::HashMap;

use crate::logic::{ApplicationSettings, TrackableModel, TrackableWithChildrenModel};

use super::*;

#[derive(Facet, Serialize, Deserialize, Clone, Debug, Default)]
#[repr(C)]
pub enum QueryResponse {
    #[default]
    None,
    AllTrackables(Vec<TrackableModel>),
    UserTrackables(Vec<TrackableModel>),
    Occurrences(HashMap<u32, u32>),
    Clicked(u32),
    DeletedOccurrence(u32),
    AddedUserTrackable,
    DeletedUserTrackable(u32),
    EditedTrackable,
    Details(TrackableWithChildrenModel),
    Settings(ApplicationSettings),
}

#[derive(Facet, Serialize, Deserialize, Clone, Debug)]
#[repr(C)]
pub enum Event {
    // Shell shared events
    /// Load all resources on first load
    Initialize,
    Query(QueryRequest),

    // Core only events
    #[serde(skip)]
    #[facet(skip)]
    Error(String),
    #[serde(skip)]
    #[facet(skip)]
    QueryResponse(QueryResponse),
}
