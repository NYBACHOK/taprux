CREATE TABLE trackables (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE,
    svg_icon TEXT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    edited_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    user_enabled BOOLEAN NOT NULL DEFAULT true,

    parent_id INTEGER NULL,
    FOREIGN KEY(parent_id) REFERENCES trackables(id)
);

CREATE TABLE trackable_occurs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    trackable_id INTEGER NOT NULL,
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY(trackable_id) REFERENCES trackables(id)
);