use std::collections::HashMap;

use serde::{Deserialize, Serialize};
use sqlx::sqlite::SqliteConnection;
use time::OffsetDateTime;

#[derive(Debug, Serialize, Deserialize, sqlx::FromRow)]
pub struct RawTrackable {
    pub id: i32,
    pub order_key: i32,
    pub name: String,
    pub svg_icon: String,
    pub created_at: OffsetDateTime,
    pub edited_at: OffsetDateTime,
    pub sub_events_count: i32,
}

#[derive(Debug, Serialize, Deserialize, sqlx::FromRow)]
pub struct RawTrackableWithChildren {
    pub id: i32,
    pub name: String,
    pub svg_icon: String,
    pub created_at: OffsetDateTime,
    pub edited_at: OffsetDateTime,
    pub sub_events: Vec<RawTrackable>,
}

/// Lists all trackables
pub async fn trackable(
    mut e: impl AsMut<SqliteConnection>,
    offset: u32,
    limit: u32,
) -> Result<Vec<RawTrackable>, sqlx::Error> {
    sqlx::query_as::<_, RawTrackable>(
        r#"
        SELECT 
        e.id, e.name, e.svg_icon, e.created_at, e.edited_at, 0 AS order_key,
        COUNT(child.id) AS sub_events_count
        FROM trackables e
        LEFT JOIN trackables child ON child.parent_id = e.id
        WHERE e.parent_id IS NULL
        GROUP BY e.id
        ORDER BY sub_events_count, e.name
        LIMIT $1 OFFSET $2;"#,
    )
    .bind(limit)
    .bind(offset)
    .fetch_all(e.as_mut())
    .await
}

pub async fn user_trackables(
    mut e: impl AsMut<SqliteConnection>,
) -> Result<Vec<RawTrackable>, sqlx::Error> {
    sqlx::query_as::<_, RawTrackable>(
        r#"
        SELECT 
            e.id, e.name, e.svg_icon, u.created_at, u.edited_at, u.order_key,
            (SELECT COUNT(*) FROM trackables WHERE parent_id = e.id) AS sub_events_count
        FROM trackables e
        JOIN user_trackables u ON u.trackable_id = e.id
        WHERE e.parent_id IS NULL
        ORDER BY u.order_key ASC"#,
    )
    .fetch_all(e.as_mut())
    .await
}

pub async fn user_trackables_add(
    mut e: impl AsMut<SqliteConnection>,
    trackable_id: u32,
) -> Result<(), sqlx::Error> {
    #[derive(sqlx::FromRow)]
    struct Raw {
        count: u32,
    }

    let mut order_key = sqlx::query_as::<_, Raw>("SELECT COUNT(*) as count FROM user_trackables")
        .fetch_one(e.as_mut())
        .await?
        .count;

    let trackable_ids = sqlx::query_scalar::<_, i32>(
        r#"
        SELECT id FROM trackables WHERE id = $1
        UNION ALL
        SELECT id FROM trackables WHERE parent_id = $1
        ORDER BY id;
        "#,
    )
    .bind(trackable_id)
    .fetch_all(e.as_mut())
    .await?;

    for trackable_id in trackable_ids {
        let existing_count: i32 =
            sqlx::query_scalar("SELECT COUNT(*) FROM user_trackables WHERE trackable_id = $1")
                .bind(trackable_id)
                .fetch_one(e.as_mut())
                .await?;

        if existing_count == 0 {
            sqlx::query("INSERT INTO user_trackables (trackable_id, order_key) VALUES ($1, $2);")
                .bind(trackable_id)
                .bind(order_key)
                .execute(e.as_mut())
                .await?;
            order_key += 1;
        }
    }

    Ok(())
}

pub async fn trackable_occurrences(
    mut e: impl AsMut<SqliteConnection>,
) -> Result<HashMap<i32, i32>, sqlx::Error> {
    #[derive(sqlx::FromRow)]
    struct Raw {
        id: i32,
        count: i32,
    }

    let occurrences = sqlx::query_as::<_, Raw>(
        r#"
        SELECT 
            e.id, (SELECT COUNT(*) FROM trackable_occurs WHERE trackable_id = e.id AND DATE(recorded_at) = DATE('now')) AS count
        FROM trackables e
        JOIN user_trackables u ON u.trackable_id = e.id"#,
    )
    .fetch_all(e.as_mut())
    .await?
    .into_iter().map( | this | (this.id, this.count))
    .collect();

    Ok(occurrences)
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

pub async fn trackable_occurrence_delete(
    mut e: impl AsMut<SqliteConnection>,
    id: u32,
) -> Result<(), sqlx::Error> {
    sqlx::query(
        r#"
        DELETE FROM trackable_occurs 
        WHERE rowid = (
            SELECT rowid FROM trackable_occurs 
            WHERE trackable_id = $1 
            ORDER BY recorded_at DESC 
            LIMIT 1
        );
        "#,
    )
    .bind(id)
    .execute(e.as_mut())
    .await?;

    Ok(())
}

/// Fetches a specific event and all its direct children
pub async fn trackable_with_children(
    mut e: impl AsMut<SqliteConnection>,
    trackable_id: u32,
) -> Result<RawTrackableWithChildren, sqlx::Error> {
    let RawTrackable {
        id,
        order_key: _,
        name,
        svg_icon,
        created_at,
        edited_at,
        sub_events_count: _,
    } = sqlx::query_as::<_, RawTrackable>(
        r#"
        SELECT 
            e.id, e.name, e.svg_icon, e.created_at, e.edited_at, 0 as order_key,
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
        SELECT e.id, e.name, e.svg_icon, e.created_at, e.edited_at,
            (SELECT COUNT(*) FROM trackable_occurs WHERE trackable_id = e.id AND DATE(recorded_at) = DATE('now')) AS event_occurrence,
            0 as sub_events_count,
            u.order_key
        FROM trackables e
        JOIN user_trackables u ON u.trackable_id = e.id
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
        sub_events,
    };

    Ok(res)
}

pub async fn user_trackable_delete(
    mut e: impl AsMut<SqliteConnection>,
    trackable_id: u32,
) -> Result<RawTrackable, sqlx::Error> {
    let trackable = sqlx::query_as::<_, RawTrackable>(
        r#"DELETE FROM user_trackables WHERE trackable_id = $1 RETURNING *;"#,
    )
    .bind(trackable_id)
    .fetch_one(e.as_mut())
    .await?;

    Ok(trackable)
}

pub async fn trackable_edit(
    mut e: impl AsMut<SqliteConnection>,
    trackable_id: u32,
    name: Option<&str>,
    svg_icon: Option<&[u8]>,
    order_key: u32,
) -> Result<(), sqlx::Error> {
    if let Some(name) = name {
        sqlx::query("UPDATE trackables SET name = $1 WHERE id = $2;")
            .bind(name)
            .bind(trackable_id)
            .execute(e.as_mut())
            .await?;
    }

    if let Some(svg_icon) = svg_icon {
        let svg_icon = data_encoding::BASE64.encode(svg_icon);
        sqlx::query("UPDATE trackables SET svg_icon = $1 WHERE id = $2;")
            .bind(svg_icon)
            .bind(trackable_id)
            .execute(e.as_mut())
            .await?;
    }

    sqlx::query("UPDATE user_trackables SET order_key = $1 WHERE trackable_id = $2;")
        .bind(order_key)
        .bind(trackable_id)
        .execute(e.as_mut())
        .await?;

    Ok(())
}

pub async fn trackable_sub_add_or_update_reorder(
    mut e: impl AsMut<SqliteConnection>,
    items: impl IntoIterator<Item = (u32, u32)>,
) -> Result<(), sqlx::Error> {
    for (trackable_id, order_key) in items {
        sqlx::query(
            "INSERT INTO user_trackables (trackable_id, order_key) VALUES ($1, $2)
                ON CONFLICT DO UPDATE SET order_key = $2 WHERE trackable_id = $1;",
        )
        .bind(trackable_id)
        .bind(order_key as u32)
        .execute(e.as_mut())
        .await?;
    }

    Ok(())
}
