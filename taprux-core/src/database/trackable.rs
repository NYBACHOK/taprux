use serde::{Deserialize, Serialize};
use sqlx::sqlite::SqliteConnection;
use time::OffsetDateTime;

#[derive(Debug, Serialize, Deserialize, sqlx::FromRow)]
pub struct RawTrackable {
    pub id: i32,
    pub name: String,
    pub svg_icon: String,
    pub created_at: OffsetDateTime,
    pub edited_at: OffsetDateTime,
    pub user_enabled: bool,
    pub event_occurrence: i32,
    pub sub_events_count: i32,
}

#[derive(Debug, Serialize, Deserialize, sqlx::FromRow)]
pub struct RawTrackableWithChildren {
    pub id: i32,
    pub name: String,
    pub svg_icon: String,
    pub created_at: OffsetDateTime,
    pub edited_at: OffsetDateTime,
    pub user_enabled: bool,
    pub event_occurrence: i32,
    pub sub_events: Vec<RawTrackable>,
}

/// Lists all trackables with their occurrence count for today
pub async fn trackable(
    mut e: impl AsMut<SqliteConnection>,
    offset: u32,
    limit: u32,
) -> Result<Vec<RawTrackable>, sqlx::Error> {
    sqlx::query_as::<_, RawTrackable>(
        r#"
        SELECT 
            e.id, e.name, e.svg_icon, e.created_at, e.edited_at, e.user_enabled,
            (SELECT COUNT(*) FROM trackable_occurs WHERE trackable_id = e.id AND DATE(timestamp) = DATE('now')) AS event_occurrence,
            (SELECT COUNT(*) FROM trackables WHERE parent_id = e.id) AS sub_events_count
        FROM trackables e
        WHERE e.parent_id IS NULL
        ORDER BY sub_events_count, e.name
        LIMIT $1 OFFSET $2"#,
    )
    .bind(limit)
    .bind(offset)
    .fetch_all(e.as_mut())
    .await
}

/// Records a new occurrence of an event
pub async fn trackable_occurrence_create(
    mut e: impl AsMut<SqliteConnection>,
    trackable_id: u32,
) -> Result<(), sqlx::Error> {
    sqlx::query("INSERT INTO trackable_occurs (trackable_id) VALUES ($1);")
        .bind(trackable_id)
        .bind(OffsetDateTime::now_utc())
        .execute(e.as_mut())
        .await?;

    Ok(())
}

/// Fetches a specific event and all its direct children
pub async fn trackable_with_children(
    mut e: impl AsMut<SqliteConnection>,
    trackable_id: u32,
) -> Result<RawTrackableWithChildren, sqlx::Error> {
    let RawTrackable { id, name, svg_icon, created_at, edited_at, user_enabled, event_occurrence, sub_events_count : _ } = sqlx::query_as::<_, RawTrackable>(
        r#"
        SELECT 
            e.id, e.name, e.svg_icon, e.created_at, e.edited_at, e.user_enabled,
            (SELECT COUNT(*) FROM trackable_occurs WHERE trackable_id = e.id AND DATE(timestamp) = DATE('now')) AS event_occurrence,
            (SELECT COUNT(*) FROM trackables WHERE parent_id = e.id) AS sub_events_count
        FROM trackables e
        WHERE e.parent_id IS NULL AND e.id = $1;
        "#,
    )
    .bind(trackable_id)
    .fetch_one(e.as_mut())
    .await?;

    let sub_events = sqlx::query_as::<_, RawTrackable>(
        r#"
        SELECT e.id, e.name, e.svg_icon, e.created_at, e.edited_at,e.user_enabled,
            (SELECT COUNT(*) FROM trackable_occurs WHERE trackable_id = e.id AND DATE(timestamp) = DATE('now')) AS event_occurrence,
            0 as sub_events_count
        FROM trackables e
        WHERE e.parent_id = $1;"#,
    )
    .bind(trackable_id)
    .fetch_all(e.as_mut())
    .await?;

    let res = RawTrackableWithChildren {
        id,
        name,
        svg_icon,
        created_at,
        edited_at,
        user_enabled,
        event_occurrence,
        sub_events,
    };

    Ok(res)
}
