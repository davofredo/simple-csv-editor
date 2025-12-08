package com.davofredo.csveditor.data;

import lombok.Data;

@Data
public class CsvPagination {
    private long pageIndex = 0;
    private int pageSize = 1000;
    private long totalRecords = 0;

    public long getRowStart() {
        return pageSize * pageIndex + 1;
    }

    public long getRowEnd() {
        return pageSize * (pageIndex + 1);
    }

    public long getPageCount() {
        return totalRecords / pageSize;
    }

    public boolean hasPreviousPage() {
        return pageIndex > 0;
    }

    public boolean hasNextPage() {
        return pageIndex + 1 < getPageCount();
    }
}
