use std::collections::HashMap;

use crate::logic::{ApplicationSettings, TrackableModel, TrackableWithChildrenModel};

use super::*;

#[derive(Facet, Serialize, Deserialize, Clone)]
pub struct ErrorModel {
    pub is_critical: bool,
    pub description: String,
}

#[derive(Facet, Serialize, Deserialize, Clone)]
pub struct ViewModel {
    pub error: Option<ErrorModel>,
    pub details: Option<TrackableWithChildrenModel>,
    pub all_trackables: Vec<TrackableModel>,
    pub user_trackables: Vec<TrackableModel>,
    pub occurrences: HashMap<u32, u32>,
    pub settings: ApplicationSettings,
}

impl ViewModel {
    pub fn error(error: &str) -> Self {
        Self {
            error: Some(ErrorModel {
                is_critical: false,
                description: error.to_owned(),
            }),
            details: None,
            all_trackables: Vec::new(),
            user_trackables: Vec::new(),
            settings: Default::default(),
            occurrences: Default::default(),
        }
    }
}
