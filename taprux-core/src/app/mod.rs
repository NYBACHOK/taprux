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
                match query_response {
                    QueryResponse::None => (),
                    QueryResponse::List(list) => {
                        model.list = list.into_iter().map(|this| (this.id, this)).collect()
                    }
                    QueryResponse::Clicked(id) => {
                        if let Some(item) = model.list.get_mut(&id) {
                            item.event_occurrence += 1;
                        }
                    }
                    QueryResponse::Details(detailed) => model.details = Some(detailed),
                }
                render()
            }
        }
    }

    fn view(
        &self,
        Model {
            error,
            details,
            list,
        }: &Model,
    ) -> ViewModel {
        if let Some(error) = error {
            return ViewModel::error(error);
        }

        ViewModel {
            error: None,
            details: details.to_owned(),
            list: list.values().cloned().collect(),
        }
    }
}
