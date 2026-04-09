use facet::Facet;
use serde::{Deserialize, Serialize};

use crate::database::{self, application_settings::RawApplicationSettings};

#[derive(Facet, Serialize, Deserialize, Debug, Clone)]
#[repr(C)]
pub enum WeekDay {
    Sunday,
    Monday,
}

impl AsRef<str> for WeekDay {
    fn as_ref(&self) -> &str {
        match self {
            WeekDay::Sunday => "Sunday",
            WeekDay::Monday => "Monday",
        }
    }
}

#[derive(Facet, Serialize, Deserialize, Debug, Clone)]
pub struct ApplicationSettings {
    pub app_version: String,
    pub week_start_day: WeekDay,
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

impl Default for ApplicationSettings {
    fn default() -> Self {
        Self {
            app_version: env!("CARGO_PKG_VERSION").to_owned(),
            week_start_day: WeekDay::Sunday,
            show_trackable_names: true,
            home_timezone: String::from("UTC"),
            device_timezone: String::from("America/New_York"),
            notif_insights_reports: true,
            notif_health_herstory: true,
            is_insights_activated: true,
            has_access: true,
            is_trial: false,
            status_message: None,
        }
    }
}

impl TryFrom<RawApplicationSettings> for ApplicationSettings {
    type Error = &'static str;

    fn try_from(
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
    ) -> Result<Self, Self::Error> {
        Ok(Self {
            week_start_day: match week_start_day.as_str() {
                "Monday" => WeekDay::Monday,
                "Sunday" => WeekDay::Sunday,
                _ => Err("invalid parameter received from db")?,
            },
            show_trackable_names,
            home_timezone,
            device_timezone,
            notif_insights_reports,
            notif_health_herstory,
            is_insights_activated,
            has_access,
            is_trial,
            status_message,
            ..Default::default()
        })
    }
}

pub async fn application_settings(pool: &sqlx::SqlitePool) -> anyhow::Result<ApplicationSettings> {
    let settings =
        database::application_settings::application_settings(pool.acquire().await?).await?;

    let settings = ApplicationSettings::try_from(settings).map_err(|e| anyhow::anyhow!("{e}"))?;

    Ok(settings)
}

pub async fn update_application_settings(
    pool: &sqlx::SqlitePool,
    ApplicationSettings {
        app_version: _,
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
    }: ApplicationSettings,
) -> anyhow::Result<ApplicationSettings> {
    let mut transaction = pool.begin().await?;

    let settings = database::application_settings::update_application_settings(
        &mut transaction,
        RawApplicationSettings {
            week_start_day: week_start_day.as_ref().to_owned(),
            show_trackable_names,
            home_timezone,
            device_timezone,
            notif_insights_reports,
            notif_health_herstory,
            is_insights_activated,
            has_access,
            is_trial,
            status_message,
        },
    )
    .await?;

    let settings = ApplicationSettings::try_from(settings).map_err(|e| anyhow::anyhow!("{e}"))?;

    transaction.commit().await?;

    Ok(settings)
}
