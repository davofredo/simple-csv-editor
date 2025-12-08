package com.davofredo.csveditor.config;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class RelationalDatabaseConfiguration {
    private static final String DB_FILE_NAME = "csv_editor_db";

    public static String getDatabaseUrl(CsvEditorSettings settings) {
        // Auto_server=true allows multiple connections
        return "jdbc:h2:file:" + settings.getCsvEditorHome() + File.separator + DB_FILE_NAME + ";AUTO_SERVER=TRUE";
    }

    public static void initializeDatabase(CsvEditorSettings settings) {
        String url = getDatabaseUrl(settings);
        try (Connection conn = DriverManager.getConnection(url, "sa", "")) {
            System.out.println("Database connection established successfully. DB Path: " + url);
        } catch (SQLException e) {
            System.err.println("Failed to initialize database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
