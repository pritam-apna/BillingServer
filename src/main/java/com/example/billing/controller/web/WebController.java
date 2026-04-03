package com.example.billing.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class WebController {
    
    @GetMapping("/")
    public String index() {
        return "redirect:/new-bill";
    }

    @GetMapping("/new-bill")
    public String newBill() {
        return "new-bill";
    }

    @GetMapping("/invoices/{id}/print")
    public String printInvoice(@PathVariable Long id, Model model) {
        model.addAttribute("invoiceId", id);
        return "invoice-print";
    }
}
