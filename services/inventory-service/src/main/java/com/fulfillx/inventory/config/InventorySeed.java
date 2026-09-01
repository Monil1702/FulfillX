package com.fulfillx.inventory.config;

import com.fulfillx.inventory.domain.InventoryItem;
import com.fulfillx.inventory.persistence.InventoryRepository;
import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class InventorySeed implements ApplicationRunner {
    private final InventoryRepository inventory;

    public InventorySeed(InventoryRepository inventory) {
        this.inventory = inventory;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (inventory.count() == 0) {
            inventory.saveAll(List.of(
                    new InventoryItem("LAPTOP-PRO", "Developer Laptop", 12),
                    new InventoryItem("MECH-KEYBOARD", "Mechanical Keyboard", 40),
                    new InventoryItem("USB-C-DOCK", "USB-C Dock", 25),
                    new InventoryItem("NOISE-HEADSET", "Noise-cancelling Headset", 18)
            ));
        }
    }
}

