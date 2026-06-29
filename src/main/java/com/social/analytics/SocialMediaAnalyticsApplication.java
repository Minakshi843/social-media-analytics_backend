package com.social.analytics;

import com.social.analytics.model.User;
import com.social.analytics.dto.CityRequest;
import com.social.analytics.dto.TargetRequest;
import com.social.analytics.model.City;
import com.social.analytics.repository.UserRepository;
import com.social.analytics.service.CityService;
import com.social.analytics.service.TargetService;
import com.social.analytics.service.SocialIntegrationService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

@SpringBootApplication
@EnableScheduling
@EnableJpaRepositories("com.social.analytics.repository")
@EntityScan("com.social.analytics.model")
public class SocialMediaAnalyticsApplication {

    public static void main(String[] args) {
        SpringApplication.run(SocialMediaAnalyticsApplication.class, args);
    }

    @Bean
    public CommandLineRunner initData(UserRepository userRepository,
                                     PasswordEncoder passwordEncoder,
                                     CityService cityService,
                                     TargetService targetService,
                                     SocialIntegrationService socialIntegrationService,
                                     DataSource dataSource) {
        return args -> {
            // Run native SQL to alter table if columns are missing (PostgreSQL fallback)
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE social_accounts ADD COLUMN IF NOT EXISTS connection_status varchar(50) NOT NULL DEFAULT 'NOT_CONNECTED';");
                stmt.execute("ALTER TABLE social_accounts ADD COLUMN IF NOT EXISTS account_handle varchar(150);");
                stmt.execute("ALTER TABLE social_accounts ADD COLUMN IF NOT EXISTS access_token text;");
                stmt.execute("ALTER TABLE social_accounts ADD COLUMN IF NOT EXISTS refresh_token text;");
                stmt.execute("ALTER TABLE social_accounts ADD COLUMN IF NOT EXISTS token_expiry timestamp;");
                stmt.execute("ALTER TABLE social_accounts ADD COLUMN IF NOT EXISTS updated_at timestamp DEFAULT CURRENT_TIMESTAMP;");
            } catch (SQLException e) {
                System.err.println("Migration warning: " + e.getMessage());
            }
            
            // 1. Create Default Users (Super Admin, Admin, Standard User) if they don't exist
            if (userRepository.findByUsername("superadmin").isEmpty()) {
                User superadmin = new User("superadmin", "superadmin@analytics.com", passwordEncoder.encode("superadmin123"), "ROLE_SUPERADMIN");
                userRepository.save(superadmin);
            }
            
            User admin = userRepository.findByUsername("admin").orElse(null);
            if (admin == null) {
                admin = new User("admin", "admin@analytics.com", passwordEncoder.encode("admin123"), "ROLE_ADMIN");
                userRepository.save(admin);
            } else if (admin.getEmail() == null) {
                admin.setEmail("admin@analytics.com");
                userRepository.save(admin);
            }

            if (userRepository.findByUsername("user").isEmpty()) {
                User userObj = new User("user", "user@analytics.com", passwordEncoder.encode("user123"), "ROLE_USER");
                userRepository.save(userObj);
            }
            
            // 2. Create Default Cities and Targets if none exist
            if (cityService.getAllCities().isEmpty()) {
                City pcmc = cityService.createCity(new CityRequest("PCMC"));
                City pune = cityService.createCity(new CityRequest("Pune"));
                
                // Seed targets for PCMC
                targetService.saveOrUpdateTarget(pcmc.getId(), new TargetRequest("INSTAGRAM", 5, 2, 3, 10));
                targetService.saveOrUpdateTarget(pcmc.getId(), new TargetRequest("FACEBOOK", 4, 1, 2, 7));
                targetService.saveOrUpdateTarget(pcmc.getId(), new TargetRequest("LINKEDIN", 2, 1, 1, 4));
                targetService.saveOrUpdateTarget(pcmc.getId(), new TargetRequest("X", 6, 0, 0, 6));

                // Seed targets for Pune
                targetService.saveOrUpdateTarget(pune.getId(), new TargetRequest("INSTAGRAM", 6, 3, 4, 13));
                targetService.saveOrUpdateTarget(pune.getId(), new TargetRequest("FACEBOOK", 5, 2, 3, 10));
                targetService.saveOrUpdateTarget(pune.getId(), new TargetRequest("LINKEDIN", 3, 1, 2, 6));
                targetService.saveOrUpdateTarget(pune.getId(), new TargetRequest("X", 8, 0, 0, 8));

                // Connect simulated accounts for PCMC and Pune
                socialIntegrationService.connectAccount(pcmc.getId(), "INSTAGRAM", "PCMC Instagram", "https://instagram.com/pcmc");
                socialIntegrationService.connectAccount(pcmc.getId(), "FACEBOOK", "PCMC Facebook Page", "https://facebook.com/pcmc");
                socialIntegrationService.connectAccount(pcmc.getId(), "LINKEDIN", "PCMC LinkedIn Company", "https://linkedin.com/company/pcmc");
                socialIntegrationService.connectAccount(pcmc.getId(), "X", "PCMC X Profile", "https://x.com/pcmc");

                socialIntegrationService.connectAccount(pune.getId(), "INSTAGRAM", "Pune Corp Instagram", "https://instagram.com/punecorp");
                socialIntegrationService.connectAccount(pune.getId(), "FACEBOOK", "Pune Corp Facebook Page", "https://facebook.com/punecorp");
                
                // Sync to populate dummy posts database
                socialIntegrationService.syncAllAccounts();
            }
        };
    }
}
