package com.example.inventoryserver.controller;

import com.example.inventoryserver.service.InventoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class InventoryWebController {

    private final InventoryService inventoryService;

    public InventoryWebController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/")
    public String dashboard(Model model) {
        model.addAttribute("items", inventoryService.findAllItems());
        model.addAttribute("transactions", inventoryService.findRecentTransactions());
        return "inventory/dashboard";
    }

    @GetMapping("/receive")
    public String receiveForm() {
        return "inventory/receive";
    }

    @PostMapping("/receive")
    public String handleReceive(@RequestParam Long productId, 
                                @RequestParam String itemName,
                                @RequestParam Integer quantity,
                                @RequestParam String reason) {
        inventoryService.adjustStock(productId, itemName, quantity, reason);
        return "redirect:/?success=true";
    }
}
