package com.davofredo.service;

import com.davofredo.command.EditCommand;
import com.davofredo.csveditor.service.DatabaseService;
import java.util.Stack;

public class UndoService {
    private static UndoService instance;
    private final Stack<EditCommand> undoStack = new Stack<>();
    private final int MAX_HISTORY = 50;

    private UndoService() {
    }

    public static UndoService getInstance() {
        if (instance == null) {
            instance = new UndoService();
        }
        return instance;
    }

    public void addCommand(EditCommand command) {
        if (undoStack.size() >= MAX_HISTORY) {
            undoStack.remove(0); // Remove oldest
        }
        undoStack.push(command);
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public EditCommand undo() {
        if (undoStack.isEmpty())
            return null;

        EditCommand cmd = undoStack.pop();
        // Revert DB change
        DatabaseService.getInstance().updateCell(
                cmd.getTableName(),
                cmd.getColumnName(),
                cmd.getRowId(),
                cmd.getOldValue());
        return cmd;
    }
}
