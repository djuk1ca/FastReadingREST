package com.example.demo.api.dto;

import java.sql.Date;
import java.util.List;

public record DailyWordsReadReportResponse(
    Date date,
    long grandTotalWords,
    List<DailyWordsReadRow> rows
) {}