package com.davofredo.csveditor.data;

import lombok.Data;

import java.util.List;

@Data
public class CsvStringData {
    private String filePath;
    private boolean containsHeader = true;
    private String[] header;
    private List<String[]> data;
    private CsvPagination pagination;
}
