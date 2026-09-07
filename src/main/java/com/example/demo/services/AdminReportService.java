package com.example.demo.services;

import java.io.InputStream;
import java.sql.Date;
import java.time.*;
import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.api.dto.DailyWordsReadReportResponse;
import com.example.demo.api.dto.DailyWordsReadRow;
import com.example.demo.repositories.ReadingSessionRepository;
import com.example.demo.repositories.projections.DailyWordsReadProjection;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

@Service
public class AdminReportService {

    private final ReadingSessionRepository sessionRepo;

    public AdminReportService(ReadingSessionRepository sessionRepo) {
        this.sessionRepo = sessionRepo;
    }

    private DailyWordsReadReportResponse getDailyWordsRead(LocalDate day) {
        LocalDateTime from = day.atStartOfDay();
        LocalDateTime to = day.plusDays(1).atStartOfDay();

        List<DailyWordsReadProjection> rows = sessionRepo.sumWordsReadByUserForDay(from, to);

        List<DailyWordsReadRow> mapped = rows.stream()
            .map(r -> new DailyWordsReadRow(
                r.getUserId(),
                r.getUsername(),
                r.getEmail(),
                r.getTotalWords() == null ? 0L : r.getTotalWords()
            ))
            .toList();

        Long grandTotal = mapped.stream().mapToLong(DailyWordsReadRow::totalWords).sum();

        return new DailyWordsReadReportResponse(Date.valueOf(day), grandTotal, mapped);
    }

    public byte[] getDailyWordsReadPdf(LocalDate day) {
        DailyWordsReadReportResponse report = getDailyWordsRead(day);

        try (InputStream jrxml = getClass().getResourceAsStream("/reports/procitanihReciPoKorisnikuUDanu.jrxml")) {
            if (jrxml == null) {
                throw new IllegalStateException("Missing Jasper template: /reports/procitanihReciPoKorisnikuUDanu.jrxml");
            }

            JasperReport jasperReport = JasperCompileManager.compileReport(jrxml);

            var params = new HashMap<String, Object>();
            params.put("reportDate", report.date().toString());
            params.put("grandTotalWords", report.grandTotalWords());

            JRBeanCollectionDataSource ds = new JRBeanCollectionDataSource(report.rows());
            JasperPrint print = JasperFillManager.fillReport(jasperReport, params, ds);

            return JasperExportManager.exportReportToPdf(print);

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate report PDF: " + e.getMessage(), e);
        }
    }
}