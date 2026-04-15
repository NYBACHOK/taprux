use std::collections::HashMap;

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
    pub order_key: u32,
    pub name: String,
    pub svg_icon: Vec<u8>,
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
            order_key,
            name,
            svg_icon,
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
            order_key: order_key.try_into()?,
            name,
            svg_icon,
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

pub async fn user_list(pool: &sqlx::SqlitePool) -> anyhow::Result<Vec<TrackableModel>> {
    let connection = pool.acquire().await?;

    let events = database::trackable::user_trackables(connection)
        .await
        .inspect_err(|e| tracing::error!(error = %e))?
        .into_iter()
        .filter_map(|this| TryFrom::try_from(this).ok())
        .collect::<Vec<TrackableModel>>();

    Ok(events)
}

pub async fn user_trackables_add(pool: &sqlx::SqlitePool, id: u32) -> anyhow::Result<()> {
    let connection = pool.acquire().await?;

    database::trackable::user_trackables_add(connection, id)
        .await
        .inspect_err(|e| tracing::error!(error = %e))?;

    Ok(())
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

pub async fn occurrences(pool: &sqlx::SqlitePool) -> anyhow::Result<HashMap<u32, u32>> {
    let connection = pool.acquire().await?;

    let occurrences = database::trackable::trackable_occurrences(connection)
        .await?
        .into_iter()
        .filter_map(
            |(id, count)| match (u32::try_from(id).ok(), u32::try_from(count).ok()) {
                (Some(id), Some(count)) => Some((id, count)),
                _ => None,
            },
        )
        .collect();

    Ok(occurrences)
}

pub async fn occurrence_delete(pool: &sqlx::SqlitePool, id: u32) -> anyhow::Result<()> {
    let mut transaction = pool.begin().await?;

    database::trackable::trackable_occurrence_delete(&mut transaction, id).await?;

    transaction.commit().await?;

    Ok(())
}
