package com.shirin.twofactorauthentication.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
public class LoginController {

    @GetMapping("/showLoginForm")
    public String showLoginForm(){
        return "login-page";
    }

    @GetMapping("/ott/request")
    public String requestTokenPage( Authentication authentication, Model model) {

        model.addAttribute("username", authentication.getName());
        return "ott-request";
    }

    @GetMapping("/verify-ott")
    public String verifyOttPage() {
        return "verify-ott";
    }

}
