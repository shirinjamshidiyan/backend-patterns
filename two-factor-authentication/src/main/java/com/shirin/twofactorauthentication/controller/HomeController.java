package com.shirin.twofactorauthentication.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {


    @GetMapping("/admin")
    public String adminHome(Authentication authentication, Model model) {
        model.addAttribute("username", authentication.getName());
        return "admin-home";
    }

    @GetMapping("/manager")
    public String managerHome(Authentication authentication, Model model) {
        model.addAttribute("username", authentication.getName());
        return "manager-home";
    }

    @GetMapping("/user")
    public String userHome(Authentication authentication, Model model) {
        model.addAttribute("username", authentication.getName());
        return "user-home";
    }
}
