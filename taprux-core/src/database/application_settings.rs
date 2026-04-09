use sqlx::SqliteConnection;

#[derive(Debug, sqlx::FromRow)]
pub struct RawApplicationSettings {
    pub week_start_day: String,
    pub show_trackable_names: bool,
    pub home_timezone: String,
    pub device_timezone: String,
    pub notif_insights_reports: bool,
    pub notif_health_herstory: bool,
    pub is_insights_activated: bool,
    pub has_access: bool,
    pub is_trial: bool,
    pub status_message: Option<String>,
}

pub async fn application_settings(
    mut e: impl AsMut<SqliteConnection>,
) -> Result<RawApplicationSettings, sqlx::Error> {
    sqlx::query_as::<_, RawApplicationSettings>(r#"SELECT * FROM application_settings;"#)
        .fetch_one(e.as_mut())
        .await
}

pub async fn update_application_settings(
    mut e: impl AsMut<SqliteConnection>,
    RawApplicationSettings {
        week_start_day,
        show_trackable_names,
        home_timezone,
        device_timezone,
        notif_insights_reports,
        notif_health_herstory,
        is_insights_activated,
        has_access,
        is_trial,
        status_message,
    }: RawApplicationSettings,
) -> Result<RawApplicationSettings, sqlx::Error> {
    let settings = sqlx::query_as::<_, RawApplicationSettings>(
        r#"
        UPDATE application_settings
        SET 
            week_start_day = $1,
            show_trackable_names = $2,
            home_timezone = $3,
            device_timezone = $4,
            notif_insights_reports = $5,
            notif_health_herstory = $6,
            is_insights_activated = $7,
            has_access = $8,
            is_trial = $9,
            status_message = $10
        WHERE id = 1 RETURNING *;
        "#,
    )
    .bind(week_start_day)
    .bind(show_trackable_names)
    .bind(home_timezone)
    .bind(device_timezone)
    .bind(notif_insights_reports)
    .bind(notif_health_herstory)
    .bind(is_insights_activated)
    .bind(has_access)
    .bind(is_trial)
    .bind(status_message)
    .fetch_one(e.as_mut())
    .await?;

    Ok(settings)
}
