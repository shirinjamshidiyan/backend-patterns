package com.shirin.twofactorauthentication.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RoleBasedAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        String username = authentication.getName();
        var authorities = authentication.getAuthorities();
        String redirectUrl = "/user";

        System.out.println(" Login successful for user: " + username);

        if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER"))) {
            redirectUrl = "/manager";
        }
        else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            redirectUrl = "/admin";
        }

        response.sendRedirect(redirectUrl);

    }
}
