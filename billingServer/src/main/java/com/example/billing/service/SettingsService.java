package com.example.billing.service;

import com.example.billing.entity.AppSetting;
import com.example.billing.repository.AppSettingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class SettingsService {

    private static final Logger log = LoggerFactory.getLogger(SettingsService.class);
    private final AppSettingRepository repository;

    @Value("${app.inventory.enabled:false}")
    private boolean inventoryEnabled;

    @Value("${app.eventing.enabled:false}")
    private boolean eventingEnabled;

    public SettingsService(AppSettingRepository repository) {
        this.repository = repository;
        log.info("SettingsService initialized. Inventory Enabled: {}, Eventing Enabled: {}", inventoryEnabled, eventingEnabled);
    }

    public String getSetting(String key, String defaultValue) {
        return repository.findById(key)
                .map(AppSetting::getSettingValue)
                .orElse(defaultValue);
    }

    public void setSetting(String key, String value) {
        repository.save(new AppSetting(key, value));
    }

    public BigDecimal getTaxRate() {
        String rateString = getSetting("TAX_RATE", "0.10"); // Default 10%
        try {
            return new BigDecimal(rateString);
        } catch (Exception e) {
            return new BigDecimal("0.10");
        }
    }

    public void setTaxRate(BigDecimal rate) {
        setSetting("TAX_RATE", rate.toString());
    }

    public boolean isInventoryEnabled() {
        log.debug("Checking if inventory is enabled: {}", inventoryEnabled);
        return inventoryEnabled;
    }

    public boolean isEventingEnabled() {
        log.debug("Checking if eventing is enabled: {}", eventingEnabled);
        return eventingEnabled;
    }

    public void setInventoryEnabled(boolean enabled) {
        setSetting("ENABLE_INVENTORY_MODULE", String.valueOf(enabled));
    }
}
