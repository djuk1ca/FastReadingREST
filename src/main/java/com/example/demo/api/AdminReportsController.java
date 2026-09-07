package com.example.demo.api;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.services.AdminReportService;

@RestController
@RequestMapping("/admin/reports")
public class AdminReportsController {

	@Autowired
	AdminReportService reportService;


    @GetMapping("/daily-words.pdf")
    public ResponseEntity<byte[]> dailyWordsPdf(@RequestParam String date) {
        LocalDate day = LocalDate.parse(date);
        byte[] pdf = reportService.getDailyWordsReadPdf(day);

        String filename = "UkupnoReciPoKorisnikuZaDan" + day + ".pdf";

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
            .body(pdf);
    }
}
