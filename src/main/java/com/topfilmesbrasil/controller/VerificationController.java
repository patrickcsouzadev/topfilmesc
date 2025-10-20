package com.topfilmesbrasil.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class VerificationController {

    @GetMapping("/email-verified")
    public String emailVerified() {
        return "email-verified";
    }

    @GetMapping("/email-verification-error")
    public String emailVerificationError() {
        return "email-verification-error";
    }

    @GetMapping("/forgot-password")
    public String forgotPassword() {
        return "forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPassword(@RequestParam(required = false) String token) {
        return "reset-password";
    }
}

