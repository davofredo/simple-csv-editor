# TODO

List of planned features and technical improvements.

## Features
- [ ] **Row Management**: Add (+) and Remove (-) rows functionality.
- [ ] **Error Handling**: Provide detailed feedback to the user when reading malformed/unreadable CSV files.
- [ ] **Search & Replace**: Add ability to find text within specific columns.
- [ ] **Filtering**: Allow users to filter rows based on column values (SQL `WHERE` clause generation).
- [ ] **Sorting**: Click column headers to sort data (`ORDER BY`).
- [ ] **Column Management**: Add/Rename/Delete columns (currently only rows are managed).
- [ ] **Data Types**: Enforce data types (Integer, Date) instead of treating everything as String.

## Technical Improvements
- [ ] **Unit Tests**: Add JUnit tests for `CsvExportService` and `UndoService`.
- [ ] **Themes**: Support FlatLaf or other Swing themes for better aesthetics.
- [ ] **Packaging**: Create a native installer (MSI/EXE) using `jpackage`.
