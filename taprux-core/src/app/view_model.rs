use crate::logic::{TrackableModel, TrackableWithChildrenModel};

use super::*;

#[derive(Facet, Serialize, Deserialize, Clone)]
pub struct ErrorModel {
    pub is_critical: bool,
    pub description: String,
}

#[derive(Facet, Serialize, Deserialize, Clone)]
#[repr(C)]
pub struct ViewModel {
    pub error: Option<ErrorModel>,
    pub details: Option<TrackableWithChildrenModel>,
    pub list: Vec<TrackableModel>,
}

impl ViewModel {
    pub fn error(error: &str) -> Self {
        Self {
            error: Some(ErrorModel {
                is_critical: false,
                description: error.to_owned(),
            }),
            details: None,
            list: Vec::new(),
        }
    }
}
