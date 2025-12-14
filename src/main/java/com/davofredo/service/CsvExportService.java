package com.davofredo.service;

import com.davofredo.csveditor.data.CsvExportOptions;
import com.davofredo.csveditor.data.QuoteMode;
import com.davofredo.event.listener.ProgressListener;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

public class CsvExportService {

    public void exportCsv(File target, Iterator<String[]> dataStream, String[] header,
            CsvExportOptions options, long totalRecords, ProgressListener listener) throws IOException {

        long writtenRecords = 0;
        long startTime = System.currentTimeMillis();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(target))) {
            // Write header if present
            if (header != null && header.length > 0) {
                writeLine(writer, header, options.getSeparator(), options.getHeaderQuoteMode());
            }

            // Write data
            while (dataStream.hasNext()) {
                String[] row = dataStream.next();
                writeLine(writer, row, options.getSeparator(), options.getDataQuoteMode());

                writtenRecords++;

                // Progress update
                if (listener != null) {
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - startTime >= 1000) {
                        float progress = (float) writtenRecords / totalRecords;
                        listener.onProgressUpdate(progress);
                        startTime = currentTime;
                    }
                }
            }

            if (listener != null)
                listener.onProgressUpdate(1.0f);
        }
    }

    private void writeLine(BufferedWriter writer, String[] row, String separator, QuoteMode mode) throws IOException {
        for (int i = 0; i < row.length; i++) {
            if (i > 0)
                writer.write(separator);
            writer.write(processField(row[i], separator, mode));
        }
        writer.newLine();
    }

    private String processField(String field, String separator, QuoteMode mode) {
        if (field == null)
            return "";

        switch (mode) {
            case ALWAYS:
                return "\"" + escapeQuotes(field) + "\"";
            case NEVER:
                return field; // Raw dump as requested
            case AS_NEEDED:
            default:
                boolean containsSep = field.contains(separator);
                boolean containsQuote = field.contains("\"");
                boolean containsNewline = field.contains("\n") || field.contains("\r");

                if (containsSep || containsQuote || containsNewline) {
                    return "\"" + escapeQuotes(field) + "\"";
                }
                return field;
        }
    }

    private String escapeQuotes(String field) {
        return field.replace("\"", "\"\"");
    }
}
