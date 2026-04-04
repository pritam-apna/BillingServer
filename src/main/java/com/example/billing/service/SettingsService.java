package com.example.billing.service;

import com.example.billing.entity.AppSetting;
import com.example.billing.repository.AppSettingRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class SettingsService {

    private final AppSettingRepository repository;

    public SettingsService(AppSettingRepository repository) {
        this.repository = repository;
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
        return "true".equalsIgnoreCase(getSetting("ENABLE_INVENTORY_MODULE", "false"));
    }

    public void setInventoryEnabled(boolean enabled) {
        setSetting("ENABLE_INVENTORY_MODULE", String.valueOf(enabled));
    }
}
