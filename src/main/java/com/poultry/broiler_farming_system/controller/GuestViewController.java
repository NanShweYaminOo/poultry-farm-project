package com.poultry.broiler_farming_system.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

// Guest accounts: informational/reference pages only, plus a read-only
// marketplace and the ability to request an upgrade to Farmer (surfaced on
// the profile page). SecurityConfig restricts "/guest/**" to ROLE_GUEST, so
// a Farmer or Admin token cannot render these even by URL guessing.
@Controller
@RequestMapping("/guest")
public class GuestViewController {

    @GetMapping("")
    public String home() {
        return "guest/dashboard"; // WEB-INF/jsp/guest/dashboard.jsp
    }

    @GetMapping("/breeds")
    public String breeds() {
        return "guest/breeds";
    }

    @GetMapping("/diseases")
    public String diseases() {
        return "guest/diseases";
    }

    @GetMapping("/faqs")
    public String faqs() {
        return "guest/faqs";
    }

    @GetMapping("/articles")
    public String articles() {
        return "guest/articles";
    }

    @GetMapping("/marketplace")
    public String marketplace() {
        return "guest/marketplace";
    }

    @GetMapping("/feedback")
    public String feedback() {
        return "guest/feedback";
    }

    @GetMapping("/profile")
    public String profile() {
        return "guest/profile";
    }
}
