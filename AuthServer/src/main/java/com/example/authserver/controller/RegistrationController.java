package com.example.authserver.controller;

import com.example.authserver.dto.ClientRequest;
import com.example.authserver.dto.UserRequest;
import com.example.authserver.service.RegistrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.UUID;

@RestController
@RequestMapping("/api/register")
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("/user")
    public ResponseEntity<String> registerUser(@RequestBody UserRequest userRequest) {
        boolean success = registrationService.registerUser(userRequest);
        if (!success) {
            return ResponseEntity.badRequest().body("Username already exists");
        }
        return ResponseEntity.ok("User registered successfully");
    }

    @PutMapping("/user/{id}")
    public ResponseEntity<String> updateUser(@PathVariable Long id, @RequestBody UserRequest userRequest) {
        boolean success = registrationService.updateUser(id, userRequest);
        if (!success) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok("User updated successfully");
    }

    @PostMapping("/client")
    public ResponseEntity<String> registerClient(@RequestBody ClientRequest clientRequest) {
        registrationService.registerClient(clientRequest);
        return ResponseEntity.ok("Client registered successfully");
    }
}
