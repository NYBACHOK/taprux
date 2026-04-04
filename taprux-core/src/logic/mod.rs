mod events;

use crate::model::Model;

pub use self::events::*;

#[derive(Debug)]
pub enum Commands {
    Events(EventCommands),
}

async fn command_handler(cmd: Commands, state: &Model) -> anyhow::Result<()> {
    match cmd {
        Commands::Events(cmd) => events::handle(cmd, state).await?,
    };

    Ok(())
}
