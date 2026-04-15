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
            Event::Initialize => Command::all([
                Command::event(Event::Query(QueryRequest::AllTrackables)),
                Command::event(Event::Query(QueryRequest::UserTrackables)),
                Command::event(Event::Query(QueryRequest::Occurrences)),
            ])
            .then(render()),
            Event::Error(error) => {
                model.error = Some(error);
                Command::event(Event::Initialize)
            }
            Event::Query(query_request) => Command::request_from_shell(query_request)
                .map(|this| match this {
                    QueryResult::Response(query) => Event::QueryResponse(query),
                    QueryResult::Err(err) => Event::Error(err),
                })
                .then_notify(|event| NotificationBuilder::new(async |ctx| ctx.send_event(event)))
                .build(),
            Event::QueryResponse(query_response) => {
                match query_response {
                    QueryResponse::None => (),
                    QueryResponse::AllTrackables(list) => {
                        model.all_list = list.into_iter().map(|this| (this.id, this)).collect()
                    }
                    QueryResponse::UserTrackables(list) => {
                        model.user_list = list.into_iter().map(|this| (this.id, this)).collect()
                    }
                    QueryResponse::Clicked(id) => {
                        if let Some(item) = model.occurrences.get_mut(&id) {
                            *item += 1;
                        } else {
                            model.occurrences.insert(id, 1);
                        }
                    }
                    QueryResponse::Details(detailed) => model.details = Some(detailed),
                    QueryResponse::Settings(settings) => model.settings = settings,
                    QueryResponse::DeletedOccurrence(id) => {
                        if let Some(item) = model.occurrences.get_mut(&id) {
                            *item = item.checked_sub(1).unwrap_or_default();
                        }
                    }
                    QueryResponse::AddedUserTrackable => {
                        return Command::event(Event::Query(QueryRequest::UserTrackables))
                            .and(Command::notify_shell(AppliedChanges::UserTrackable).build());
                    }
                    QueryResponse::Occurrences(occurrences) => model.occurrences = occurrences,
                }
                render()
            }
        }
    }

    fn view(&self, model: &Model) -> ViewModel {
        let Model {
            error,
            details,
            all_list,
            user_list,
            settings,
            occurrences,
        } = model.to_owned();

        if let Some(error) = error {
            return ViewModel::error(&error);
        }

        let all_trackables = all_list.into_values().collect::<Vec<_>>();
        let mut user_trackables = user_list.into_values().collect::<Vec<_>>();

        user_trackables.sort_by_key(|this| this.order_key);

        ViewModel {
            error: None,
            details: details.to_owned(),
            all_trackables,
            user_trackables,
            settings,
            occurrences,
        }
    }
}
