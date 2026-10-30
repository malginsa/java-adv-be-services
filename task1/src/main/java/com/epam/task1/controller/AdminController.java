package com.epam.task1.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @GetMapping("/cms")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminResource() {
        return "Admin page stub";
    }
}
