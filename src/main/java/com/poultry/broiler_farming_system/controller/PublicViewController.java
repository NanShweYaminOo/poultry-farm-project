package com.poultry.broiler_farming_system.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PublicViewController {

    @GetMapping("/login")
    public String login() {
        return "public/login"; // WEB-INF/jsp/public/login.jsp
    }

    @GetMapping("/register")
    public String register() {
        return "public/register"; // WEB-INF/jsp/public/register.jsp
    }
}
