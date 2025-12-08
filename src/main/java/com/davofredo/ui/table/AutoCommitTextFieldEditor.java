package com.davofredo.ui.table;

import javax.swing.table.TableCellEditor;
import javax.swing.JTextField;
import javax.swing.JTable;
import javax.swing.AbstractCellEditor;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.Component;
import java.util.EventObject;

public class AutoCommitTextFieldEditor extends AbstractCellEditor implements TableCellEditor {

    private JTextField currentField;

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        // Create a NEW component for every edit to prevent state bleeding between cells
        currentField = new JTextField();
        currentField.setText(value != null ? value.toString() : "");

        // Setup listeners for the new field
        currentField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    stopCellEditing();
                    e.consume();
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    cancelCellEditing();
                    e.consume();
                }
            }
        });

        return currentField;
    }

    @Override
    public Object getCellEditorValue() {
        return currentField != null ? currentField.getText() : "";
    }

    @Override
    public boolean isCellEditable(EventObject anEvent) {
        return true;
    }

    @Override
    public boolean shouldSelectCell(EventObject anEvent) {
        return true;
    }

}
