package com.davofredo.csveditor.ui.sheet;

import com.davofredo.csveditor.data.CsvStringData;
import com.davofredo.csveditor.service.DatabaseService;
import com.davofredo.service.UndoService;
import com.davofredo.toolbox.SecuentialAccumulator;
import com.davofredo.ui.table.AutoCommitTextFieldEditor;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;
import java.awt.Color;
import javax.swing.table.TableCellRenderer;
import java.awt.Component;

import com.davofredo.command.EditCommand;

public class SheetView extends JScrollPane {

    private JTable table;
    private CsvStringData csvData;

    private static JTable initJTable() {
        // DefaultTableModel model = new DefaultTableModel(new Object[]{"Columna 1",
        // "Columna 2", "Columna 3"}, 3);
        DefaultTableModel model = new DefaultTableModel(new Object[] { "A", "B", "C" }, 0);
        JTable table = new JTable(model) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row,
                    int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (getSelectedRow() == row) {
                    c.setBackground(new Color(220, 240, 255));
                } else {
                    c.setBackground(Color.WHITE);
                }
                return c;
            }
        };

        table.setDefaultEditor(Object.class, new AutoCommitTextFieldEditor());
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        table.setSelectionBackground(new Color(220, 240, 255));
        table.setSelectionForeground(Color.BLACK);
        table.setRowSelectionAllowed(true);
        table.setColumnSelectionAllowed(true);
        table.setCellSelectionEnabled(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        return table;
    }

    public SheetView() {
        initComponents();
    }

    private void initComponents() {
        table = initJTable();
        setViewportView(table);
    }

    public void setCsvData(CsvStringData csvData) {
        this.csvData = csvData;

        // Create new tracking model
        EditTrackingTableModel tableModel = new EditTrackingTableModel(getColumnIdentifiers(csvData.getHeader()), 0);
        table.setModel(tableModel);

        int prevColumnCount = table.getColumnModel().getColumnCount();
        final var acum = new SecuentialAccumulator(csvData.getPagination().getRowStart());
        csvData.getData()
                // .stream()
                // .skip(1)
                .forEach(r -> tableModel.addRow(getDataRow(r, acum.getNext())));

        if (table.getColumnModel().getColumnCount() != prevColumnCount)
            resizeTableColumns();
    }

    // Custom Model to intercept edits
    private class EditTrackingTableModel extends DefaultTableModel {
        private static final long serialVersionUID = 1L;

        public EditTrackingTableModel(Object[] columnNames, int rowCount) {
            super(columnNames, rowCount);
        }

        @Override
        public void setValueAt(Object aValue, int row, int column) {
            if (column == 0) { // ID column is read-only
                super.setValueAt(aValue, row, column);
                return;
            }

            String oldVal = (String) getValueAt(row, column);
            String newVal = (String) aValue;

            if ((oldVal == null && newVal == null) || (oldVal != null && oldVal.equals(newVal))) {
                return; // No change
            }

            super.setValueAt(aValue, row, column);

            // Persist & Undo Logic
            try {
                String rowId = (String) getValueAt(row, 0);
                String colName = getColumnName(column);
                String tableName = DatabaseService.getInstance().getTableName(
                        new java.io.File(csvData.getFilePath()));

                // Update DB
                DatabaseService.getInstance().updateCell(tableName, colName, rowId,
                        newVal);

                // Push Undo
                UndoService.getInstance().addCommand(new EditCommand(
                        tableName, rowId, colName, oldVal, newVal, row, column));
            } catch (Exception e) {
                System.err.println("Error persisting edit: " + e.getMessage());
                e.printStackTrace();
            }
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return column > 0; // ID not editable
        }
    }

    private String[] getColumnIdentifiers(String[] columnIdentifiers) {
        var newIdentifiers = new String[columnIdentifiers.length + 1];
        newIdentifiers[0] = " ";
        System.arraycopy(columnIdentifiers, 0, newIdentifiers, 1, columnIdentifiers.length);
        return newIdentifiers;
    }

    private String[] getDataRow(String[] row, long rowNum) {
        var newRow = new String[row.length];
        System.arraycopy(row, 0, newRow, 0, row.length);
        return newRow;
    }

    private void resizeTableColumns() {
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        for (var i = 1; i < table.getColumnModel().getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(100);
            // table.getColumnModel().getColumn(i).setWidth(200);
        }
    }

}
