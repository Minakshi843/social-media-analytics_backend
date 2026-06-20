package com.social.analytics.controller;

import com.social.analytics.dto.TargetRequest;
import com.social.analytics.model.Target;
import com.social.analytics.service.TargetService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/targets")
public class TargetController {

    private final TargetService targetService;

    @Autowired
    public TargetController(TargetService targetService) {
        this.targetService = targetService;
    }

    @GetMapping("/city/{cityId}")
    public ResponseEntity<List<Target>> getTargetsByCity(@PathVariable Long cityId) {
        return ResponseEntity.ok(targetService.getTargetsByCity(cityId));
    }

    @PostMapping("/city/{cityId}")
    public ResponseEntity<Target> saveOrUpdateTarget(@PathVariable Long cityId,
                                                     @Valid @RequestBody TargetRequest request) {
        return ResponseEntity.ok(targetService.saveOrUpdateTarget(cityId, request));
    }
}
