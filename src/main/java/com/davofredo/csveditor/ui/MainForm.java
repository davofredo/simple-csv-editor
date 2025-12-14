/*
 * Created by JFormDesigner on Fri Jan 03 12:15:46 CST 2025
 */

package com.davofredo.csveditor.ui;

import com.davofredo.csveditor.data.CsvExportOptions;
import com.davofredo.csveditor.data.CsvPagination;
import com.davofredo.csveditor.data.CsvStringData;
import com.davofredo.csveditor.service.DatabaseService;
import com.davofredo.csveditor.ui.toolbar.NavigationBar;
import com.davofredo.event.EventGateway;
import com.davofredo.event.EventGatewayImpl;
import com.davofredo.csveditor.ui.dialog.ExportConfigDialog;
import com.davofredo.csveditor.ui.sheet.SheetView;
import com.davofredo.csveditor.ui.toolbar.MainToolBar;
import com.davofredo.csveditor.ui.toolbar.event.OpenFileButtonListener;
import com.davofredo.csveditor.ui.toolbar.event.RedoButtonListener;
import com.davofredo.csveditor.ui.toolbar.event.SaveButtonListener;
import com.davofredo.ui.dialog.ProgressDialog;

// Import javax.swing classes individually to avoid loading unused classes
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;

import java.awt.BorderLayout;
// Import java.awt classes individually to avoid loading unused classes
import java.awt.event.WindowAdapter;
import java.io.File;
import java.awt.Toolkit;

import com.davofredo.csveditor.ui.toolbar.event.UndoButtonListener;
import com.davofredo.service.CsvExportService;
import com.davofredo.service.UndoService;

/**
 * @author davof
 */
public class MainForm extends JFrame implements OpenFileButtonListener, UndoButtonListener,
        RedoButtonListener, SaveButtonListener {
    private static final String APP_NAME = "CSV Editor";

    private MainToolBar toolBar;
    private NavigationBar navBar;
    private SheetView sheetView;
    private EventGateway eventGateway;
    private CsvStringData csvData;

    public MainForm() {
        super(APP_NAME);
        setupEventManagement();
        initComponents();
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                DatabaseService.getInstance().shutdown();
            }
        });
    }

    private void initComponents() {
        // Controls
        sheetView = new SheetView();
        toolBar = new MainToolBar(eventGateway);
        navBar = new NavigationBar(new CsvPagination());

        navBar.addNextPageListener(e -> {
            var next = csvData.getPagination().getPageIndex() + 1;
            csvData.getPagination().setPageIndex(next);
            populateDataTable(false);
        });

        navBar.addPreviousPageListener(e -> {
            var prev = csvData.getPagination().getPageIndex() - 1;
            csvData.getPagination().setPageIndex(Math.max(prev, 0));
            populateDataTable(false);
        });

        navBar.addJumpToPageListener(pageIndex -> {
            csvData.getPagination().setPageIndex(pageIndex);
            populateDataTable(false);
        });

        // Layout
        var contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        // Add controls
        contentPane.add(toolBar, BorderLayout.PAGE_START);
        contentPane.add(sheetView, BorderLayout.CENTER);
        contentPane.add(navBar, BorderLayout.PAGE_END);

        pack();
        setLocationRelativeTo(getOwner());
    }

    private void setupEventManagement() {
        eventGateway = new EventGatewayImpl();
        eventGateway.addEventListener(this, OpenFileButtonListener.class);
        eventGateway.addEventListener(this, UndoButtonListener.class);
        eventGateway.addEventListener(this, RedoButtonListener.class);
        eventGateway.addEventListener(this, SaveButtonListener.class);
    }

    public void onOpenFileButtonClicked() {
        JFileChooser j = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        j.setAcceptAllFileFilterUsed(false);
        j.setDialogTitle("Select a CSV file");
        FileNameExtensionFilter restrict = new FileNameExtensionFilter("Only .csv files", "csv");
        j.addChoosableFileFilter(restrict);

        int r = j.showOpenDialog(this);
        if (r == JFileChooser.APPROVE_OPTION) {
            File file = j.getSelectedFile();
            setTitle(APP_NAME + " - " + file.getName());
            loadFile(file);
        }
    }

    public void onUndoButtonClicked() {
        var cmd = UndoService.getInstance().undo();
        if (cmd != null) {
            // Reload current page to show changes
            populateDataTable(false);
            // Optional: Select the undone cell
            // sheetView.selectCell(cmd.getRowIndex(), cmd.getColIndex()); // Method not
            // implemented yet
        } else {
            Toolkit.getDefaultToolkit().beep(); // beep if empty
        }
    }

    public void onRedoButtonClicked() {
        var cmd = UndoService.getInstance().redo();
        if (cmd != null) {
            populateDataTable(false);
        } else {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    public void onSaveButtonClicked() {
        // 1. Configuration Dialog
        ExportConfigDialog configDialog = new ExportConfigDialog(this);
        configDialog.setVisible(true);
        var options = configDialog.getOptions();

        if (options == null) {
            return; // User cancelled
        }

        // 2. File Selection
        JFileChooser j = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        j.setDialogTitle("Save CSV As");
        j.setSelectedFile(new File("export.csv"));
        FileNameExtensionFilter restrict = new FileNameExtensionFilter("Only .csv files", "csv");
        j.addChoosableFileFilter(restrict);

        int r = j.showSaveDialog(this);
        if (r == JFileChooser.APPROVE_OPTION) {
            File file = j.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".csv")) {
                file = new File(file.getParentFile(), file.getName() + ".csv");
            }
            exportCsv(file, options);
        }
    }

    private void exportCsv(File file, CsvExportOptions options) {
        var progressDialog = new ProgressDialog(this, "Exporting...")
                .doInBackground(pl -> {
                    try {
                        var dbService = DatabaseService.getInstance();
                        var exportService = new CsvExportService();
                        String tableName = dbService.getTableName(new File(csvData.getFilePath()));

                        // Get header
                        String[] header = dbService.getHeader(tableName);

                        // Get count for progress
                        long totalRecords = dbService.countRecords(tableName);

                        try (var dataStream = dbService.streamAllData(tableName)) {
                            exportService.exportCsv(file, dataStream, header, options, totalRecords, pl);
                        }

                        System.out.println("Exported to " + file.getAbsolutePath());
                    } catch (Exception e) {
                        System.err.println("Export failed: " + e.getMessage());
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                });

        progressDialog.addWorkerPropertyChangeListener(e -> {
            if ("state".equals(e.getPropertyName()) && SwingWorker.StateValue.DONE == e.getNewValue()) {
                progressDialog.dispose();
                javax.swing.JOptionPane.showMessageDialog(this, "Export completed successfully!");
            }
        }).start();
    }

    private void loadFile(File file) {
        var pagination = new CsvPagination();
        csvData = new CsvStringData();
        csvData.setFilePath(file.getAbsolutePath());
        csvData.setPagination(pagination);
        navBar.updateComponents(pagination);
        importCsvRecords(file); // then populateDataTable
    }

    private void populateDataTable(boolean needToLoadHeader) {
        if (needToLoadHeader)
            loadHeader();
        loadPage();
        sheetView.setCsvData(csvData);
        navBar.updateComponents();
        System.out.println("Page " + (csvData.getPagination().getPageIndex() + 1) + " loaded");
    }

    private void importCsvRecords(File file) {
        var progressDialog = new ProgressDialog(this, "Importing...")
                .doInBackground(pl -> {
                    try {
                        var dbService = DatabaseService.getInstance();
                        dbService.importCsv(file, pl);

                        String tableName = dbService.getTableName(file);
                        long rowCount = dbService.countRecords(tableName);

                        csvData.getPagination().setTotalRecords(rowCount);
                        System.out.println("Imported " + rowCount + " records to table " + tableName);
                    } catch (Exception e) {
                        System.err.println("CSV import failed with error: " + e.getMessage());
                        e.printStackTrace();
                    }
                });
        progressDialog.addWorkerPropertyChangeListener(e -> {
            if ("state".equals(e.getPropertyName()) && SwingWorker.StateValue.DONE == e.getNewValue()) {
                progressDialog.dispose();
                populateDataTable(true);
            }
        }).start();
    }

    private void loadHeader() {
        File file = new File(csvData.getFilePath());
        if (!csvData.isContainsHeader())
            return;
        try {
            var dbService = DatabaseService.getInstance();
            String tableName = dbService.getTableName(file);
            csvData.setHeader(dbService.getHeader(tableName));
        } catch (Exception e) {
            System.err.println("Header load failed: " + e.getMessage());
        }
    }

    private void loadPage() {
        File file = new File(csvData.getFilePath());
        var page = csvData.getPagination();
        try {
            var dbService = DatabaseService.getInstance();
            String tableName = dbService.getTableName(file);
            csvData.setData(dbService.loadPage(tableName, page.getPageIndex(), page.getPageSize()));
        } catch (Exception e) {
            System.err.println("Page load failed: " + e.getMessage());
        }
    }

}
