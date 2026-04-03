package com.example.billing.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminWebController {
    
    @GetMapping({"", "/"})
    public String dashboard() {
        return "admin/dashboard";
    }

    @GetMapping("/products")
    public String products() {
        return "admin/products";
    }

    @GetMapping("/settings")
    public String settings() {
        return "admin/settings";
    }
}
