package com.example.security.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/")
    public String home(Authentication auth, Model model) {
        model.addAttribute("email", auth.getName());
        model.addAttribute("authorities", auth.getAuthorities());
        return "home";
    }
}
