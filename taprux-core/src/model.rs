pub struct Model {
    pub pool: sqlx::SqlitePool,
}

impl Model {
    pub fn new(pool: sqlx::SqlitePool) -> Self {
        Self { pool }
    }
}
