package com.example.inventoryserver.config;

import com.example.inventoryserver.entity.InventoryItem;
import com.example.inventoryserver.repository.InventoryItemRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

@Configuration
public class MockDataConfig {

    private static final Logger log = LoggerFactory.getLogger(MockDataConfig.class);

    @Bean
    public CommandLineRunner initInventoryData(InventoryItemRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                log.info("Seeding Initial Inventory Items...");
                repository.saveAll(List.of(
                    new InventoryItem(1L, "Laptop Pro", 100),
                    new InventoryItem(2L, "Wireless Mouse", 100),
                    new InventoryItem(3L, "Ergonomic Keyboard", 100),
                    new InventoryItem(4L, "USB-C Hub", 100),
                    new InventoryItem(5L, "HD Monitor 27\"", 100)
                ));
                log.info("Initial Inventory Items Seeded Successfully!");
            } else {
                log.info("Inventory items currently exist in database.");
                // Ensure existing elements have names if they were null
                List<InventoryItem> items = repository.findAll();
                boolean updated = false;
                for (InventoryItem item : items) {
                    if (item.getItemName() == null) {
                        String name = switch (item.getProductId().intValue()) {
                            case 1 -> "Laptop Pro";
                            case 2 -> "Wireless Mouse";
                            case 3 -> "Ergonomic Keyboard";
                            case 4 -> "USB-C Hub";
                            case 5 -> "HD Monitor 27\"";
                            default -> "Unknown Product";
                        };
                        item.setItemName(name);
                        updated = true;
                    }
                }
                if (updated) {
                    repository.saveAll(items);
                    log.info("Updated exiting inventory items with their correct names!");
                }
            }
        };
    }
}
