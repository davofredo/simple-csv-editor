package com.davofredo.file.util;

import com.davofredo.event.listener.ProgressListener;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class FileUtils {
    private static final long PROGRESS_UPDATE_RATE_MILLIS = 1000;

    private FileUtils() {}

    public static Charset detectCharsetFromFile(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] bom = new byte[4];
            int readBytes = fis.read(bom);
            if(readBytes < 2) return null;

            if (bom[0] == (byte) 0xEF && bom[1] == (byte) 0xBB && bom[2] == (byte) 0xBF) {
                return StandardCharsets.UTF_8;
            } else if (bom[0] == (byte) 0xFE && bom[1] == (byte) 0xFF) {
                return StandardCharsets.UTF_16BE;
            } else if (bom[0] == (byte) 0xFF && bom[1] == (byte) 0xFE) {
                return StandardCharsets.UTF_16LE;
            } else if (readBytes == 4 && bom[0] == (byte) 0x00 && bom[1] == (byte) 0x00 && bom[2] == (byte) 0xFE && bom[3] == (byte) 0xFF) {
                return Charset.forName("UTF-32BE");
            } else if (readBytes == 4 && bom[0] == (byte) 0xFF && bom[1] == (byte) 0xFE && bom[2] == (byte) 0x00 && bom[3] == (byte) 0x00) {
                return Charset.forName("UTF-32LE");
            }
        }
        return null;
    }

    public static long countFileLines(File file, ProgressListener progressListener, boolean excludeBlanks) throws IOException {
        long totalFileLength = file.length();
        long readBytes = 0;
        long lineCount = 0;
        long startTime = System.currentTimeMillis();

        var charset = FileUtils.detectCharsetFromFile(file);
        var charsetName = charset == null ? "UTF-8" : charset.name();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (excludeBlanks && line.trim().isBlank()) continue; // Don't count blank lines
                lineCount++;
                readBytes += line.getBytes(charsetName).length + System.lineSeparator().getBytes(charsetName).length;

                long elapsedTime = System.currentTimeMillis() - startTime;
                if (elapsedTime >= PROGRESS_UPDATE_RATE_MILLIS) {
                    float progress = (float) readBytes / (float) totalFileLength;
                    progressListener.onProgressUpdate(progress);
                    startTime = System.currentTimeMillis();
                }
            }
        }

        progressListener.onProgressUpdate(1f);
        return lineCount;
    }

}
