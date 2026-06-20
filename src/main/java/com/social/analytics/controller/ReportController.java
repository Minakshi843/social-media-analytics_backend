package com.social.analytics.controller;

import com.social.analytics.exception.ResourceNotFoundException;
import com.social.analytics.model.Report;
import com.social.analytics.repository.ReportRepository;
import com.social.analytics.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;
    private final ReportRepository reportRepository;

    @Autowired
    public ReportController(ReportService reportService, ReportRepository reportRepository) {
        this.reportService = reportService;
        this.reportRepository = reportRepository;
    }

    @GetMapping
    public ResponseEntity<List<Report>> getAllReports() {
        return ResponseEntity.ok(reportService.getAllReports());
    }

    @PostMapping("/generate")
    public ResponseEntity<Report> generateReport(@RequestParam String type,
                                                 @RequestParam String format,
                                                 @RequestParam(required = false) Long cityId) {
        try {
            Report report = reportService.generateReport(type, cityId, format);
            return ResponseEntity.status(HttpStatus.CREATED).body(report);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate report: " + e.getMessage(), e);
        }
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadReport(@PathVariable Long id) {
        try {
            Report report = reportRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Report not found with id: " + id));

            byte[] data = reportService.getReportFile(id);
            String filename = Paths.get(report.getFilePath()).getFileName().toString();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentDispositionFormData("attachment", filename);
            
            if (filename.toLowerCase().endsWith(".pdf")) {
                headers.setContentType(MediaType.APPLICATION_PDF);
            } else if (filename.toLowerCase().endsWith(".xlsx")) {
                headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            } else {
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            }

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(data);
        } catch (Exception e) {
            throw new RuntimeException("Failed to download file: " + e.getMessage(), e);
        }
    }
}
