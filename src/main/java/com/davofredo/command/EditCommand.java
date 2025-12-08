package com.davofredo.command;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EditCommand {
    private final String tableName;
    private final String rowId;
    private final String columnName;
    private final String oldValue;
    private final String newValue;
    private final int rowIndex;
    private final int colIndex;
}
