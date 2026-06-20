package com.social.analytics.service;

import com.social.analytics.model.Report;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SyncScheduler {

    private static final Logger logger = LoggerFactory.getLogger(SyncScheduler.class);

    private final SocialIntegrationService socialIntegrationService;
    private final ReportService reportService;
    private final WhatsAppService whatsAppService;

    @Autowired
    public SyncScheduler(SocialIntegrationService socialIntegrationService,
                         ReportService reportService,
                         WhatsAppService whatsAppService) {
        this.socialIntegrationService = socialIntegrationService;
        this.reportService = reportService;
        this.whatsAppService = whatsAppService;
    }

    /**
     * Runs every hour (at the top of the hour) to sync social accounts.
     * Cron expression: "0 0 * * * *"
     */
    @Scheduled(cron = "0 0 * * * *")
    public void runHourlySync() {
        logger.info("Scheduler triggered: Hourly Social Media Sync execution starting.");
        try {
            socialIntegrationService.syncAllAccounts();
            logger.info("Scheduler triggered: Hourly Sync executed successfully.");
        } catch (Exception e) {
            logger.error("Error occurred during hourly scheduled sync", e);
        }
    }

    /**
     * Runs every day at 8:00 PM.
     * Generates a PDF report and triggers a simulated WhatsApp Business API notification.
     * Cron expression: "0 0 20 * * *"
     */
    @Scheduled(cron = "0 0 20 * * *")
    public void runDailyReportAndWhatsAppDispatch() {
        logger.info("Scheduler triggered: 8:00 PM Daily Reporting & WhatsApp Dispatch starting.");
        try {
            // Generate daily report (Global - for all cities)
            Report report = reportService.generateReport("DAILY", null, "PDF");
            logger.info("Daily PDF report compiled successfully at: {}", report.getFilePath());

            // Dispatch to admin (simulated number)
            String targetPhone = "+919876543210";
            boolean success = whatsAppService.sendPdfReport(targetPhone, report.getFilePath(), "DAILY");
            
            if (success) {
                logger.info("Automated daily WhatsApp notification dispatched successfully to {}", targetPhone);
            } else {
                logger.warn("Automated daily WhatsApp notification failed to dispatch.");
            }
        } catch (Exception e) {
            logger.error("Error occurred during 8 PM scheduled reporting and WhatsApp dispatch", e);
        }
    }
}
