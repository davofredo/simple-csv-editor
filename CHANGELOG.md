# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]
- Row Management: Add (+) and Remove (-) rows functionality.
- Error Handling: Provide detailed feedback to the user when reading malformed/unreadable CSV files.
- Search and Replace functionality.
- Column sorting and filtering.
- Dark mode theme support.

## [1.0.0] - 2025-12-14
### Added
- **CSV Export**: Fully configurable export (separators, quoting rules) with efficient streaming.
- **Undo/Redo**: Robust history stack for cell edits.
- **Large File Support**: H2 Database integration for pagination and disk-based storage.
- **Navigation**: "Jump to Page" feature and pagination controls.
- **UI**: Row highlighting and responsive configuration dialogs.
- **Save**: Validated export with specific quoting rules ("Always", "As Needed", "Never").

### Changed
- Optimized CSV import with batch processing (5000 records/batch).
- Refactored `DatabaseService` to use connection pooling.
- Decoupled export logic into `CsvExportService`.
