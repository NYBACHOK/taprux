CREATE TABLE application_settings (
    id INTEGER PRIMARY KEY CHECK (id = 1),
    
    week_start_day TEXT NOT NULL DEFAULT 'Sunday',
    show_trackable_names INTEGER NOT NULL DEFAULT 1,
    home_timezone TEXT NOT NULL DEFAULT 'UTC',
    device_timezone TEXT NOT NULL DEFAULT 'America/New_York',
    notif_insights_reports INTEGER NOT NULL DEFAULT 1,
    notif_health_herstory INTEGER NOT NULL DEFAULT 1,
    is_insights_activated INTEGER NOT NULL DEFAULT 1,
    has_access INTEGER NOT NULL DEFAULT 1,
    is_trial INTEGER NOT NULL DEFAULT 0,
    status_message TEXT
);

-- Create single row for defaults
INSERT INTO application_settings (id) VALUES (1);