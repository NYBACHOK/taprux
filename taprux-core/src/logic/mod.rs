mod events;

use crate::app_state::AppState;

pub use self::events::*;

#[derive(Debug)]
pub enum Commands {
    Events(EventCommands),
}

async fn command_handler(cmd: Commands, state: &AppState) -> anyhow::Result<()> {
    match cmd {
        Commands::Events(cmd) => events::handle(cmd, state).await?,
    };

    Ok(())
}
