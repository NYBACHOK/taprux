use anyhow::Context;
use facet::Facet;
use serde::{Deserialize, Serialize};

use crate::database::{
    self,
    trackable::{RawTrackable, RawTrackableWithChildren},
};

const ELEMENTS_LIMIT: u32 = 100;

#[derive(Facet, Serialize, Deserialize, Clone, Debug)]
pub struct TrackableModel {
    pub id: u32,
    pub name: String,
    pub svg_icon: Vec<u8>,
    pub event_occurrence: u32,
    pub has_sub_events: bool,
}

#[derive(Facet, Serialize, Deserialize, Clone, Debug)]
pub struct TrackableWithChildrenModel {
    pub id: u32,
    pub name: String,
    pub svg_icon: Vec<u8>,
    pub sub_events: Vec<TrackableModel>,
}

impl TryFrom<RawTrackable> for TrackableModel {
    type Error = anyhow::Error;

    fn try_from(
        RawTrackable {
            id,
            name,
            svg_icon,
            event_occurrence,
            sub_events_count,
            created_at: _,
            edited_at: _,
        }: RawTrackable,
    ) -> Result<Self, Self::Error> {
        let svg_icon = data_encoding::BASE64
            .decode(svg_icon.as_bytes())
            .context("decoding svg icon")?;

        Ok(Self {
            id: id.try_into()?,
            name,
            svg_icon,
            event_occurrence: event_occurrence.try_into()?,
            has_sub_events: sub_events_count > 0,
        })
    }
}

impl TryFrom<RawTrackableWithChildren> for TrackableWithChildrenModel {
    type Error = anyhow::Error;

    fn try_from(
        RawTrackableWithChildren {
            id,
            name,
            svg_icon,
            sub_events,
            ..
        }: RawTrackableWithChildren,
    ) -> Result<Self, Self::Error> {
        let svg_icon = data_encoding::BASE64
            .decode(svg_icon.as_bytes())
            .context("decoding svg icon")?;

        Ok(Self {
            id: id.try_into()?,
            name,
            svg_icon,
            sub_events: sub_events
                .into_iter()
                .filter_map(|this| TryFrom::try_from(this).ok())
                .collect::<Vec<TrackableModel>>(),
        })
    }
}

pub async fn list(pool: &sqlx::SqlitePool, offset: u32) -> anyhow::Result<Vec<TrackableModel>> {
    let connection = pool.acquire().await?;

    let events = database::trackable::trackable(connection, offset, ELEMENTS_LIMIT)
        .await
        .inspect_err(|e| tracing::error!(error = %e))?
        .into_iter()
        .filter_map(|this| TryFrom::try_from(this).ok())
        .collect::<Vec<TrackableModel>>();

    Ok(events)
}

pub async fn clicked(pool: &sqlx::SqlitePool, id: u32) -> anyhow::Result<()> {
    database::trackable::trackable_occurrence_create(pool.acquire().await?, id)
        .await
        .inspect_err(|e| tracing::error!(error = %e))?;

    Ok(())
}

pub async fn details(
    pool: &sqlx::SqlitePool,
    id: u32,
) -> anyhow::Result<TrackableWithChildrenModel> {
    let connection = pool.acquire().await?;

    let event = database::trackable::trackable_with_children(connection, id)
        .await
        .inspect_err(|e| tracing::error!(error = %e))?;

    Ok(event.try_into()?)
}
