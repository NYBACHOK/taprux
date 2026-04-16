use std::collections::HashMap;

use crate::logic::{ApplicationSettings, TrackableModel, TrackableWithChildrenModel};

#[derive(Debug, Clone, Default)]
pub struct Model {
    pub details: Option<TrackableWithChildrenModel>,
    pub all_list: HashMap<u32, TrackableModel>,
    pub user_list: HashMap<u32, TrackableModel>,
    pub occurrences: HashMap<u32, u32>,
    pub settings: ApplicationSettings,
}
