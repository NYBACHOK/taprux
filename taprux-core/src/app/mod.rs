use crux_core::{
    App, Command,
    macros::effect,
    render::{RenderOperation, render},
};
use facet::Facet;
use serde::{Deserialize, Serialize};

mod capabilities;
mod effect;
mod event;
mod middleware;
mod model;
mod state;
mod view_model;

pub use self::{
    capabilities::*, effect::*, event::*, middleware::*, model::*, state::*, view_model::*,
};

#[derive(Default)]
pub struct Application;

impl App for Application {
    type Event = Event;
    type Model = Model;
    type ViewModel = ViewModel;
    type Effect = Effect;

    fn update(&self, event: Event, model: &mut Model) -> Command<Effect, Event> {
        match event {
            Event::Increment => model.count += 1,
            Event::Decrement => model.count -= 1,
            Event::Reset => model.count = 0,
        }

        // let command = Command::new(|ctx: CommandContext<Effect, Event>| async move {
        //     let handler = ctx.spawn(|_| async move {});

        //     let res = handler.await;
        // });

        render()
    }

    fn view(&self, model: &Model) -> ViewModel {
        ViewModel::Count(format!("Count is: {}", model.count))
    }
}
