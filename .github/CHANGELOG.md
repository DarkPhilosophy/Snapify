# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.0.1] - 2026-01-13
### Fixed
- **CI/CD**: Fixed release asset collision by switching to `ncipollo/release-action` and bumping version.
- **Release**: Fixed metadata generation for empty commit bodies.

## [1.0.0] - 2026-01-13
### Added
- **Edge-to-Edge UI**: Migrated legacy UI flags to `enableEdgeToEdge()` for a modern, immersive experience.
- **Project Structure**: Added `version.properties` for automated version management.

### Fixed
- **Share and Delete**: Removed arbitrary timers and implemented aggressive cache cleanup in `OverlayService` (on create & pre-share) to fix sharing failures and storage leaks.
