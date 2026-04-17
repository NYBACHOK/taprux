# Taprux

A cross-platform habit tracking application built with Rust and the Crux framework. I mainly use this project as option to learn crux and UI development with main focus on finding right way of building the applications. Why Crux? Because Rust only frameworks generally ready for PC platforms, but have limited support on mobile devices. Crux allows you to write core logic in Rust with easy integration into other langues and platforms.

## Architecture

The project is structured as a Rust workspace with the following components:

- **taprux-core**: The core Rust library containing the application logic, built using the [Crux](github.com/redbadger/crux) framework
- **taprux-android**: Android application using Jetpack Compose for the UI
- **todo**: iOS application
- **sql/**: Database schema and migration files

## Features

- Create and manage trackable habits/activities
- Hierarchical trackables with sub-events support
- Record and track occurrences
- SQLite database for local storage
- Cross-platform architecture (Android and iOS later)
