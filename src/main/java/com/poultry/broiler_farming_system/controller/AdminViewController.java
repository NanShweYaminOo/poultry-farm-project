package com.poultry.broiler_farming_system.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminViewController {

    @GetMapping("/login")
    public String showAdminLoginPage() {
        return "admin/login"; // WEB-INF/jsp/admin/login.jsp ကို ညွှန်ပြခြင်း ဖြစ်ရပါမယ်
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "admin/dashboard";
    }

    @GetMapping("/posts")
    public String posts() {
        return "admin/posts";
    }

    @GetMapping("/users")
    public String users() {
        return "admin/users";
    }

    @GetMapping("/group-chats")
    public String groupChats() {
        return "admin/group-chats";
    }

    @GetMapping("/feedback")
    public String feedback() {
        return "admin/feedback";
    }

    @GetMapping("/breeds")
    public String breeds() {
        return "admin/breeds";
    }

    @GetMapping("/diseases")
    public String diseases() {
        return "admin/diseases";
    }

    @GetMapping("/faqs")
    public String faqs() {
        return "admin/faqs";
    }

    @GetMapping("/articles")
    public String articles() {
        return "admin/articles";
    }

    @GetMapping("/logs")
    public String logs() {
        return "admin/logs";
    }

    @GetMapping("/help")
    public String help() {
        return "admin/help";
    }

    @GetMapping("/profile")
    public String profile() {
        return "admin/profile";
    }
}
