use crux_core::{
    App, Command,
    command::NotificationBuilder,
    macros::effect,
    render::{RenderOperation, render},
};
use facet::Facet;
use serde::{Deserialize, Serialize};

mod effect;
mod event;
mod middleware;
mod model;
mod state;
mod view_model;

pub use self::{effect::*, event::*, middleware::*, model::*, state::*, view_model::*};

#[derive(Default)]
pub struct Application;

impl App for Application {
    type Event = Event;
    type Model = Model;
    type ViewModel = ViewModel;
    type Effect = Effect;

    fn update(&self, event: Event, model: &mut Model) -> Command<Effect, Event> {
        match event {
            Event::Error(error) => {
                model.error = Some(error);
                render()
            }
            Event::QueryRequest(query_request) => Command::request_from_shell(query_request)
                .map(|this| match this {
                    Ok(query) => Event::QueryResponse(query),
                    Err(err) => Event::Error(err),
                })
                .then_notify(|event| NotificationBuilder::new(async |ctx| ctx.send_event(event)))
                .build(),
            Event::QueryResponse(query_response) => {
                model.query = query_response;
                render()
            }
        }
    }

    fn view(&self, model: &Model) -> ViewModel {
        if let Some(error) = &model.error {
            return ViewModel::Error(ErrorModel {
                is_critical: false,
                description: error.clone(),
            });
        }

        todo!()
    }
}
