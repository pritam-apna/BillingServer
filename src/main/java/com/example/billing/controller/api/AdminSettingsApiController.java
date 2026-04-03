package com.example.billing.controller.api;

import com.example.billing.service.SettingsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/settings")
public class AdminSettingsApiController {

    private final SettingsService settingsService;

    public AdminSettingsApiController(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @GetMapping("/tax-rate")
    public ResponseEntity<Map<String, BigDecimal>> getTaxRate() {
        return ResponseEntity.ok(Map.of("rate", settingsService.getTaxRate()));
    }

    @PutMapping("/tax-rate")
    public ResponseEntity<Void> updateTaxRate(@RequestBody Map<String, BigDecimal> payload) {
        if(payload.containsKey("rate")) {
            settingsService.setTaxRate(payload.get("rate"));
        }
        return ResponseEntity.ok().build();
    }
}
