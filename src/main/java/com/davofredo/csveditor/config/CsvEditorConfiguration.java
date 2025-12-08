package com.davofredo.csveditor.config;

import java.io.File;

public class CsvEditorConfiguration {

    private static CsvEditorSettings csvEditorSettings;

    public static synchronized CsvEditorSettings getSettings() {
        if (csvEditorSettings == null) {
            loadSettings();
        }
        return csvEditorSettings;
    }

    private static void loadSettings() {
        csvEditorSettings = new CsvEditorSettings();
        homeDirectorySetup();
    }

    private static void homeDirectorySetup() {
        // Try user home first
        String homePath = System.getProperty(GlobalConstants.SYS_PROP_USER_HOME) + File.separator
                + GlobalConstants.DEFAULT_HOME_DIR_NAME;

        if (!ensureDirectoryExists(homePath)) {
            System.err.println("Failed to create home directory at: " + homePath);

            // Fallback to temp directory
            System.err.println("Attempting fallback to temporary directory.");
            homePath = System.getProperty("java.io.tmpdir") + File.separator + GlobalConstants.DEFAULT_HOME_DIR_NAME;

            if (!ensureDirectoryExists(homePath)) {
                throw new ConfigurationAbortException(
                        "Critical: Could not create config directory in user home or temp. Aborting. Path: "
                                + homePath);
            }
        }

        csvEditorSettings.setCsvEditorHome(homePath);
        System.out.println("Configuration home set to: " + homePath);
    }

    private static boolean ensureDirectoryExists(String path) {
        File dir = new File(path);
        if (dir.exists()) {
            return dir.isDirectory();
        }
        return dir.mkdirs();
    }

}
