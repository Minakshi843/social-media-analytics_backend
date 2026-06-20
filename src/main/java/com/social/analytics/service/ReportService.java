package com.social.analytics.service;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.social.analytics.exception.ResourceNotFoundException;
import com.social.analytics.model.*;
import com.social.analytics.repository.CityRepository;
import com.social.analytics.repository.PostRepository;
import com.social.analytics.repository.ReportRepository;
import com.social.analytics.repository.TargetRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@SuppressWarnings("null")
public class ReportService {

    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);

    private final ReportRepository reportRepository;
    private final CityRepository cityRepository;
    private final PostRepository postRepository;
    private final TargetRepository targetRepository;
    private final TargetService targetService;

    private static final String REPORT_DIR = "generated-reports";

    @Autowired
    public ReportService(ReportRepository reportRepository, CityRepository cityRepository,
                         PostRepository postRepository, TargetRepository targetRepository,
                         TargetService targetService) {
        this.reportRepository = reportRepository;
        this.cityRepository = cityRepository;
        this.postRepository = postRepository;
        this.targetRepository = targetRepository;
        this.targetService = targetService;

        // Ensure reporting folder exists
        try {
            Files.createDirectories(Paths.get(REPORT_DIR));
        } catch (IOException e) {
            logger.error("Could not create reporting directory: {}", REPORT_DIR, e);
        }
    }

    public List<Report> getAllReports() {
        return reportRepository.findByOrderByGeneratedAtDesc();
    }

    public byte[] getReportFile(Long reportId) throws IOException {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with id: " + reportId));
        Path path = Paths.get(report.getFilePath());
        return Files.readAllBytes(path);
    }

    @Transactional
    public Report generateReport(String type, Long cityId, String format) throws Exception {
        City city = cityId != null ? cityRepository.findById(cityId)
                .orElseThrow(() -> new ResourceNotFoundException("City not found with id: " + cityId)) : null;

        String scope = city != null ? city.getName() : "Global";
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = scope + "_" + type + "_Report_" + timestamp + "." + format.toLowerCase();
        Path targetPath = Paths.get(REPORT_DIR, filename);

        LocalDate endDate = LocalDate.now();
        LocalDate startDate;

        if ("WEEKLY".equalsIgnoreCase(type)) {
            startDate = endDate.minusWeeks(1);
        } else if ("MONTHLY".equalsIgnoreCase(type)) {
            startDate = endDate.minusMonths(1);
        } else {
            startDate = endDate.minusDays(1); // DAILY
        }

        List<Post> posts;
        if (city != null) {
            posts = postRepository.findByCityIdAndPostDateBetween(city.getId(), startDate, endDate);
        } else {
            posts = postRepository.findByPostDateBetween(startDate, endDate);
        }

        if ("PDF".equalsIgnoreCase(format)) {
            generatePdfReport(targetPath.toString(), type, scope, startDate, endDate, posts, city);
        } else {
            generateExcelReport(targetPath.toString(), type, scope, startDate, endDate, posts, city);
        }

        Report report = new Report(type.toUpperCase() + "_" + format.toUpperCase(), targetPath.toString(), city);
        return reportRepository.save(report);
    }

    private void generatePdfReport(String filePath, String type, String scope, LocalDate start, LocalDate end, List<Post> posts, City city) throws Exception {
        Document document = new Document(PageSize.A4, 36, 36, 54, 54);
        PdfWriter.getInstance(document, new FileOutputStream(filePath));
        document.open();

        // Styles
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, Color.DARK_GRAY);
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.GRAY);
        Font textFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
        Font boldTextFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);

        // Header
        Paragraph title = new Paragraph("Social Media Analytics " + type + " Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        Paragraph metadata = new Paragraph("Scope: " + scope + " | Generated At: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n" +
                "Data Period: " + start.toString() + " to " + end.toString(), textFont);
        metadata.setAlignment(Element.ALIGN_CENTER);
        document.add(metadata);
        document.add(Chunk.NEWLINE);

        // Metrics Summary Table
        document.add(new Paragraph("1. Execution Metrics Summary", sectionFont));
        document.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);

        addPdfCell(table, "Category", boldTextFont, Color.LIGHT_GRAY, true);
        addPdfCell(table, "Total Posts", boldTextFont, Color.LIGHT_GRAY, true);
        addPdfCell(table, "Likes / Engagements", boldTextFont, Color.LIGHT_GRAY, true);
        addPdfCell(table, "Impressions", boldTextFont, Color.LIGHT_GRAY, true);

        // Aggregate Platform stats
        long likes = 0, comments = 0, impressions = 0;
        int staticCount = 0, carouselCount = 0, reelCount = 0;
        for (Post p : posts) {
            likes += p.getLikes();
            comments += p.getComments();
            impressions += p.getImpressions();

            if ("STATIC".equalsIgnoreCase(p.getPostType())) staticCount++;
            else if ("CAROUSEL".equalsIgnoreCase(p.getPostType())) carouselCount++;
            else if ("REEL".equalsIgnoreCase(p.getPostType())) reelCount++;
        }

        addPdfCell(table, "Static Posts", textFont, Color.WHITE, false);
        addPdfCell(table, String.valueOf(staticCount), textFont, Color.WHITE, false);
        addPdfCell(table, "-", textFont, Color.WHITE, false);
        addPdfCell(table, "-", textFont, Color.WHITE, false);

        addPdfCell(table, "Carousel Posts", textFont, Color.WHITE, false);
        addPdfCell(table, String.valueOf(carouselCount), textFont, Color.WHITE, false);
        addPdfCell(table, "-", textFont, Color.WHITE, false);
        addPdfCell(table, "-", textFont, Color.WHITE, false);

        addPdfCell(table, "Reel Posts", textFont, Color.WHITE, false);
        addPdfCell(table, String.valueOf(reelCount), textFont, Color.WHITE, false);
        addPdfCell(table, "-", textFont, Color.WHITE, false);
        addPdfCell(table, "-", textFont, Color.WHITE, false);

        addPdfCell(table, "Total / Summary", boldTextFont, Color.LIGHT_GRAY, true);
        addPdfCell(table, String.valueOf(posts.size()), boldTextFont, Color.LIGHT_GRAY, true);
        addPdfCell(table, String.valueOf(likes + comments), boldTextFont, Color.LIGHT_GRAY, true);
        addPdfCell(table, String.valueOf(impressions), boldTextFont, Color.LIGHT_GRAY, true);

        document.add(table);
        document.add(Chunk.NEWLINE);

        // Targets Achievement Table (if City-specific report is fetched)
        if (city != null) {
            document.add(new Paragraph("2. Target Engine Accomplishments", sectionFont));
            document.add(Chunk.NEWLINE);

            PdfPTable targetTable = new PdfPTable(6);
            targetTable.setWidthPercentage(100);

            addPdfCell(targetTable, "Platform", boldTextFont, Color.LIGHT_GRAY, true);
            addPdfCell(targetTable, "Content Type", boldTextFont, Color.LIGHT_GRAY, true);
            addPdfCell(targetTable, "Actual (30d Agg)", boldTextFont, Color.LIGHT_GRAY, true);
            addPdfCell(targetTable, "Daily Target", boldTextFont, Color.LIGHT_GRAY, true);
            addPdfCell(targetTable, "Achievement %", boldTextFont, Color.LIGHT_GRAY, true);
            addPdfCell(targetTable, "Pending", boldTextFont, Color.LIGHT_GRAY, true);

            String[] platforms = {"INSTAGRAM", "FACEBOOK", "LINKEDIN", "X"};
            for (String plat : platforms) {
                Target target = targetRepository.findByCityIdAndPlatform(city.getId(), plat).orElse(null);
                int dailyTarget = target != null ? target.getDailyPostTarget() : 0;

                long platformPosts = posts.stream().filter(p -> p.getPlatform().equalsIgnoreCase(plat)).count();
                double achievement = targetService.calculateAchievementRate(platformPosts, dailyTarget);
                int pending = targetService.calculatePending((int) platformPosts, dailyTarget);

                addPdfCell(targetTable, plat, boldTextFont, Color.WHITE, false);
                addPdfCell(targetTable, "Total Posts", textFont, Color.WHITE, false);
                addPdfCell(targetTable, String.valueOf(platformPosts), textFont, Color.WHITE, false);
                addPdfCell(targetTable, String.valueOf(dailyTarget), textFont, Color.WHITE, false);
                addPdfCell(targetTable, String.format("%.1f%%", achievement), textFont, Color.WHITE, false);
                addPdfCell(targetTable, String.valueOf(pending), textFont, Color.WHITE, false);
            }
            document.add(targetTable);
        }

        document.close();
    }

    private void generateExcelReport(String filePath, String type, String scope, LocalDate start, LocalDate end, List<Post> posts, City city) throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Posts Analytics");

        // Styling
        org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());

        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);

        // Header Row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Post ID", "Platform", "City", "Post URL", "Type", "Likes", "Comments", "Reach", "Impressions", "Post Date", "Post Time"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Data Rows
        int rowIdx = 1;
        for (Post post : posts) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(post.getPostId());
            row.createCell(1).setCellValue(post.getPlatform());
            row.createCell(2).setCellValue(post.getCity().getName());
            row.createCell(3).setCellValue(post.getPostUrl());
            row.createCell(4).setCellValue(post.getPostType());
            row.createCell(5).setCellValue(post.getLikes());
            row.createCell(6).setCellValue(post.getComments());
            row.createCell(7).setCellValue(post.getReach());
            row.createCell(8).setCellValue(post.getImpressions());
            row.createCell(9).setCellValue(post.getPostDate().toString());
            row.createCell(10).setCellValue(post.getPostTime().toString());
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        FileOutputStream out = new FileOutputStream(filePath);
        workbook.write(out);
        workbook.close();
        out.close();
    }

    private void addPdfCell(PdfPTable table, String text, Font font, Color bgColor, boolean isHeader) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bgColor);
        cell.setPadding(8);
        if (isHeader) {
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        } else {
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        }
        table.addCell(cell);
    }
}
