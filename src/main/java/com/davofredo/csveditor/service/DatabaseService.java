package com.davofredo.csveditor.service;

import com.davofredo.csveditor.config.CsvEditorConfiguration;
import com.davofredo.csveditor.config.RelationalDatabaseConfiguration;
import com.davofredo.event.listener.ProgressListener;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.h2.jdbcx.JdbcConnectionPool;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DatabaseService {
    private static final long PROGRESS_UPDATE_RATE_MILLIS = 1000;
    private static DatabaseService instance;
    private JdbcConnectionPool cp;

    private DatabaseService() {
        String url = RelationalDatabaseConfiguration.getDatabaseUrl(CsvEditorConfiguration.getSettings());
        this.cp = JdbcConnectionPool.create(url, "sa", "");
    }

    public static synchronized DatabaseService getInstance() {
        if (instance == null) {
            instance = new DatabaseService();
        }
        return instance;
    }

    public void shutdown() {
        if (cp != null) {
            cp.dispose();
        }
    }

    public void importCsv(File file, ProgressListener progressListener)
            throws IOException, SQLException, CsvValidationException {
        String tableName = sanitizeName(getTableName(file));
        long totalFileLength = file.length();
        long readBytes = 0;
        long startTime = System.currentTimeMillis();

        try (Connection conn = getConnection()) {
            // 1. Drop table if exists
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("DROP TABLE IF EXISTS " + tableName);
            }

            // 2. Read Header
            try (CSVReader reader = new CSVReader(new FileReader(file))) {
                String[] header = reader.readNext();
                if (header == null)
                    return;

                // Track bytes for header
                readBytes += String.join(",", header).length() + 1; // Approximation

                // 3. Create Table
                StringBuilder createTableSql = new StringBuilder(
                        "CREATE TABLE " + tableName + " (id IDENTITY PRIMARY KEY");
                for (String col : header) {
                    createTableSql.append(", ").append(sanitizeName(col)).append(" VARCHAR(255)");
                }
                createTableSql.append(")");

                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(createTableSql.toString());
                }

                // 4. Insert Data
                StringBuilder insertSql = new StringBuilder("INSERT INTO " + tableName + " (");
                for (int i = 0; i < header.length; i++) {
                    if (i > 0)
                        insertSql.append(", ");
                    insertSql.append(sanitizeName(header[i]));
                }
                insertSql.append(") VALUES (");
                for (int i = 0; i < header.length; i++) {
                    if (i > 0)
                        insertSql.append(", ");
                    insertSql.append("?");
                }
                insertSql.append(")");

                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql.toString())) {
                    String[] line;
                    int count = 0;
                    final int BATCH_SIZE = 5000;

                    while ((line = reader.readNext()) != null) {
                        // Skip empty lines or lines with wrong column count
                        if (line.length != header.length)
                            continue;

                        // Track bytes (Approximation: sum of chars + commas)
                        long lineBytes = 0;
                        for (int i = 0; i < line.length; i++) {
                            insertStmt.setString(i + 1, line[i]);
                            lineBytes += line[i].length();
                        }
                        insertStmt.addBatch();
                        count++;

                        if (count % BATCH_SIZE == 0) {
                            insertStmt.executeBatch();
                        }

                        readBytes += lineBytes + line.length; // +commas

                        long elapsedTime = System.currentTimeMillis() - startTime;
                        if (elapsedTime >= PROGRESS_UPDATE_RATE_MILLIS && progressListener != null) {
                            float progress = (float) readBytes / (float) totalFileLength;
                            progressListener.onProgressUpdate(progress);
                            startTime = System.currentTimeMillis();
                        }
                    }
                    insertStmt.executeBatch(); // Insert remaining records
                }
            }
        }
        if (progressListener != null) {
            progressListener.onProgressUpdate(1f);
        }
    }

    public long countRecords(String tableName) {
        String sql = "SELECT COUNT(*) FROM " + sanitizeName(tableName);
        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting records: " + e.getMessage());
        }
        return 0;
    }

    public List<String[]> loadPage(String tableName, long pageIdx, int pageSize) {
        List<String[]> rows = new ArrayList<>();
        String sanitizedTable = sanitizeName(tableName);
        long startId = pageIdx * pageSize;
        // Optimization: Use ID index (WHERE id > ?) instead of OFFSET for O(1)
        // performance
        // This assumes IDs are roughly sequential.
        String sql = "SELECT * FROM " + sanitizedTable + " WHERE id > ? LIMIT ?";

        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, startId);
            stmt.setInt(2, pageSize);

            try (ResultSet rs = stmt.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                int colCount = meta.getColumnCount();

                while (rs.next()) {
                    // Skip column 1 (ID)
                    String[] row = new String[colCount];
                    for (int i = 1; i <= colCount; i++) {
                        row[i - 1] = rs.getString(i);
                    }
                    rows.add(row);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading page: " + e.getMessage());
        }
        return rows;
    }

    public void updateCell(String tableName, String columnName, String rowId, String newValue) {
        String sanitizedTable = sanitizeName(tableName);
        String sanitizedCol = sanitizeName(columnName);
        String sql = "UPDATE " + sanitizedTable + " SET " + sanitizedCol + " = ? WHERE id = ?";

        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newValue);
            stmt.setLong(2, Long.parseLong(rowId));
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating cell: " + e.getMessage());
        }
    }

    public String[] getHeader(String tableName) {
        String sanitizedTable = sanitizeName(tableName);
        String sql = "SELECT * FROM " + sanitizedTable + " LIMIT 1";
        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();
            String[] header = new String[colCount - 1];
            // Skip ID
            for (int i = 2; i <= colCount; i++) {
                header[i - 2] = meta.getColumnName(i);
            }
            return header;
        } catch (SQLException e) {
            System.err.println("Error reading header: " + e.getMessage());
        }
        return new String[0];
    }

    public String getTableName(File file) {
        return file.getName().replace(".csv", "").replace(".CSV", "");
    }

    private Connection getConnection() throws SQLException {
        return cp.getConnection();
    }

    public interface CloseableIterator<E> extends Iterator<E>, AutoCloseable {
        @Override
        void close();
    }

    public CloseableIterator<String[]> streamAllData(String tableName) {
        String sanitizedTable = sanitizeName(tableName);
        String sql = "SELECT * FROM " + sanitizedTable; // Iterate full table

        try {
            Connection conn = getConnection();
            Statement stmt = conn.createStatement(); // Create statement
            // Set fetch size to avoid loading all into memory if driver supports it (H2
            // usually does reasonably well)
            // But strict streaming might need specific H2 config.
            // Default H2 behavior with simple query is usually okay for forward-only RS.
            ResultSet rs = stmt.executeQuery(sql);
            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();

            return new CloseableIterator<String[]>() {
                // Must advance next manually to know if 'next' exists for Iterator contract
                Boolean hasNext = null;

                @Override
                public boolean hasNext() {
                    if (hasNext == null) {
                        try {
                            hasNext = rs.next();
                        } catch (SQLException e) {
                            e.printStackTrace();
                            hasNext = false;
                            close();
                        }
                    }
                    return hasNext;
                }

                @Override
                public String[] next() {
                    if (hasNext == null)
                        hasNext(); // Ensure state
                    if (!hasNext) {
                        throw new java.util.NoSuchElementException();
                    }

                    try {
                        String[] row = new String[colCount - 1];
                        // Skip ID (col 1), so start at 2
                        for (int i = 2; i <= colCount; i++) {
                            row[i - 2] = rs.getString(i);
                        }
                        hasNext = null; // Reset for next checking
                        return row;
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void close() {
                    try {
                        rs.close();
                    } catch (SQLException e) {
                    }
                    try {
                        stmt.close();
                    } catch (SQLException e) {
                    }
                    try {
                        conn.close();
                    } catch (SQLException e) {
                    }
                }
            };

        } catch (SQLException e) {
            e.printStackTrace();
            return new CloseableIterator<String[]>() {
                @Override
                public boolean hasNext() {
                    return false;
                }

                @Override
                public String[] next() {
                    throw new java.util.NoSuchElementException();
                }

                @Override
                public void close() {
                }
            };
        }
    }

    private String sanitizeName(String input) {
        if (input == null)
            return "unknown";
        // Replace spaces with underscores
        String sanitized = input.trim().replace(" ", "_");
        // Remove non-alphanumeric (except underscores)
        sanitized = sanitized.replaceAll("[^a-zA-Z0-9_]", "");
        // Ensure it doesn't start with a number (valid SQL identifier rule mostly)
        if (sanitized.matches("^[0-9].*")) {
            sanitized = "_" + sanitized;
        }
        return sanitized;
    }
}
