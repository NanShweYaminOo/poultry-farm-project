package com.poultry.broiler_farming_system.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

// Farmer accounts: full operational toolset (batches, daily logs, alarms,
// medicine calculator, inventory, premium/payment requests) plus the same
// reference pages Guests get, and a marketplace that additionally allows
// posting (gated server-side by SalesPostServiceImpl.requirePostingPrivilege).
// SecurityConfig restricts "/farmer/**" to ROLE_FARMER.
@Controller
@RequestMapping("/farmer")
public class FarmerViewController {

    @GetMapping("")
    public String home() {
        return "farmer/dashboard"; // WEB-INF/jsp/farmer/dashboard.jsp
    }

    @GetMapping("/my-batches")
    public String myBatches() {
        return "farmer/my-batches";
    }

    @GetMapping("/daily-log")
    public String dailyLog() {
        return "farmer/daily-log";
    }

    @GetMapping("/alarms")
    public String alarms() {
        return "farmer/alarms";
    }

    @GetMapping("/medicine-calculator")
    public String medicineCalculator() {
        return "farmer/medicine-calculator";
    }

    @GetMapping("/inventory")
    public String inventory() {
        return "farmer/inventory";
    }

    @GetMapping("/chatbot")
    public String chatbot() {
        return "farmer/chatbot";
    }

    @GetMapping("/group-chat")
    public String groupChat() {
        return "farmer/group-chat";
    }

    @GetMapping("/breeds")
    public String breeds() {
        return "farmer/breeds";
    }

    @GetMapping("/diseases")
    public String diseases() {
        return "farmer/diseases";
    }

    @GetMapping("/faqs")
    public String faqs() {
        return "farmer/faqs";
    }

    @GetMapping("/articles")
    public String articles() {
        return "farmer/articles";
    }

    @GetMapping("/marketplace")
    public String marketplace() {
        return "farmer/marketplace";
    }

    @GetMapping("/feedback")
    public String feedback() {
        return "farmer/feedback";
    }

    @GetMapping("/premium")
    public String premium() {
        return "farmer/premium";
    }

    @GetMapping("/profile")
    public String profile() {
        return "farmer/profile";
    }
}
