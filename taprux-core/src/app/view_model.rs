use super::*;

#[derive(Facet, Serialize, Deserialize, Clone)]
pub struct ErrorModel {
    pub is_critical: bool,
    pub description: String,
}

#[derive(Facet, Serialize, Deserialize, Clone,  )]
#[repr(C)]
pub enum ViewModel {
    Error(ErrorModel),
    Count(String)
}