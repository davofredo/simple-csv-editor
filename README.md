# Simple CSV Editor

A high-performance, desktop-based CSV editor built with Java Swing. Designed to handle large CSV files efficiently using disk-based storage and pagination.

## Key Features

### 🚀 High Performance & Scalability
- **Large File Support**: Uses an embedded H2 database to store CSV data, allowing files larger than available RAM to be opened and edited.
- **Pagination**: Data is loaded in pages to ensure the UI remains responsive regardless of file size.
- **Smart Importing/Exporting**: Uses streaming and batch processing for low memory footprint during I/O operations.

### ✍️ Powerful Editing
- **In-Cell Editing**: Double-click any cell to edit. Changes are auto-committed on save/focus loss.
- **Undo/Redo**: robust history stack allows you to revert and re-apply changes confidently.
- **Row Highlighting**: Visual indicators for the currently active row.

### 💾 Flexible Export
- **Configurable Formats**: Choose your own separator (comma, semicolon, pipe, etc.).
- **Quoting Rules**: Control how data is quoted:
    - **Always**: Max compatibility.
    - **As Needed**: Intelligent quoting only when separators or special characters are present.
    - **Never**: Raw export for specific use cases.

## Technology Stack
- **Java 21**: Core language.
- **Swing**: User Interface toolkit.
- **H2 Database**: Persistent storage engine for handling large datasets.
- **OpenCSV**: Parsing and validation library.

## Getting Started

### Prerequisites
- JDK 21 or higher.
- Maven.

### Running the Application
```bash
mvn clean compile exec:java
```

### Usage
1.  **Open**: Click the folder icon to load a CSV file.
2.  **Edit**: Modify cells directly in the grid.
3.  **Navigate**: Use the bottom pagination bar to jump between pages.
4.  **Undo/Redo**: Use the toolbar buttons to manage your edit history.
5.  **Save**: Click the floppy disk icon to export your changes with custom formatting options.
