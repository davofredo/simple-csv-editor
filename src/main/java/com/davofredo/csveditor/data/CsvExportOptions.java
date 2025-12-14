package com.davofredo.csveditor.data;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CsvExportOptions {
    private QuoteMode dataQuoteMode;
    private QuoteMode headerQuoteMode;
    private String separator;
}
