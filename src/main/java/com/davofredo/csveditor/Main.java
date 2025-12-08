package com.davofredo.csveditor;

import com.davofredo.csveditor.config.CsvEditorConfiguration;
import com.davofredo.csveditor.config.RelationalDatabaseConfiguration;
import com.davofredo.csveditor.ui.MainForm;

public class Main {
    public static void main(String[] args) {
        runInitialSetupIfNeeded();
        new MainForm().setVisible(true);
    }

    private static void runInitialSetupIfNeeded() {
        RelationalDatabaseConfiguration.initializeDatabase(
                CsvEditorConfiguration.getSettings());
    }
}