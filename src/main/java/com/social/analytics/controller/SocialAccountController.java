package com.social.analytics.controller;

import com.social.analytics.model.SocialAccount;
import com.social.analytics.service.SocialIntegrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/social-accounts")
public class SocialAccountController {

    private final SocialIntegrationService socialIntegrationService;

    @Autowired
    public SocialAccountController(SocialIntegrationService socialIntegrationService) {
        this.socialIntegrationService = socialIntegrationService;
    }

    @GetMapping
    public ResponseEntity<List<SocialAccount>> getAllAccounts() {
        return ResponseEntity.ok(socialIntegrationService.getAllAccounts());
    }

    @GetMapping("/city/{cityId}")
    public ResponseEntity<List<SocialAccount>> getAccountsByCity(@PathVariable Long cityId) {
        return ResponseEntity.ok(socialIntegrationService.getAccountsByCity(cityId));
    }

    @PostMapping("/connect")
    public ResponseEntity<SocialAccount> connectAccount(@RequestParam Long cityId,
                                                        @RequestParam String platform,
                                                        @RequestParam String accountName,
                                                        @RequestParam(required = false) String accountUrl) {
        SocialAccount account = socialIntegrationService.connectAccount(cityId, platform, accountName, accountUrl);
        return new ResponseEntity<>(account, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> disconnectAccount(@PathVariable Long id) {
        socialIntegrationService.disconnectAccount(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/sync-all")
    public ResponseEntity<String> syncAllAccounts() {
        socialIntegrationService.syncAllAccounts();
        return ResponseEntity.ok("Successfully synchronized all social channels.");
    }

    @PostMapping("/city/{cityId}/sync")
    public ResponseEntity<String> syncCityAccounts(@PathVariable Long cityId) {
        socialIntegrationService.syncCityAccounts(cityId);
        return ResponseEntity.ok("Successfully synchronized channels for City ID " + cityId);
    }
}
