package com.social.analytics.controller;

import com.social.analytics.dto.SocialAccountCreateRequest;
import com.social.analytics.dto.SocialAccountDto;
import com.social.analytics.dto.SocialAccountUpdateRequest;
import com.social.analytics.model.SocialAccount;
import com.social.analytics.service.SocialAccountService;
import com.social.analytics.service.SocialIntegrationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/social-accounts")
public class SocialAccountController {

    private final SocialAccountService socialAccountService;
    private final SocialIntegrationService socialIntegrationService;

    @Autowired
    public SocialAccountController(SocialAccountService socialAccountService,
                                   SocialIntegrationService socialIntegrationService) {
        this.socialAccountService = socialAccountService;
        this.socialIntegrationService = socialIntegrationService;
    }

    @GetMapping
    public ResponseEntity<List<SocialAccountDto>> getAllAccounts() {
        return ResponseEntity.ok(socialAccountService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SocialAccountDto> getAccountById(@PathVariable Long id) {
        return ResponseEntity.ok(socialAccountService.getById(id));
    }

    @GetMapping("/city/{cityId}")
    public ResponseEntity<List<SocialAccountDto>> getAccountsByCity(@PathVariable Long cityId) {
        return ResponseEntity.ok(socialAccountService.getByCityId(cityId));
    }

    @PostMapping
    public ResponseEntity<SocialAccountDto> createAccount(@Valid @RequestBody SocialAccountCreateRequest request) {
        SocialAccountDto created = socialAccountService.create(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SocialAccountDto> updateAccount(@PathVariable Long id,
                                                          @Valid @RequestBody SocialAccountUpdateRequest request) {
        SocialAccountDto updated = socialAccountService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long id) {
        socialAccountService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Keep connect and sync endpoints for backward compatibility with simulated seeder
    @PostMapping("/connect")
    public ResponseEntity<SocialAccount> connectAccountSimulated(@RequestParam Long cityId,
                                                                 @RequestParam String platform,
                                                                 @RequestParam String accountName,
                                                                 @RequestParam(required = false) String accountUrl) {
        SocialAccount account = socialIntegrationService.connectAccount(cityId, platform, accountName, accountUrl);
        return new ResponseEntity<>(account, HttpStatus.CREATED);
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
