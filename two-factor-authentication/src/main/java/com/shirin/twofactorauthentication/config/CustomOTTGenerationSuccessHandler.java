package com.shirin.twofactorauthentication.config;

import com.shirin.twofactorauthentication.entity.EmailDetails;
import com.shirin.twofactorauthentication.service.MailService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.ott.OneTimeToken;
import org.springframework.security.web.authentication.ott.OneTimeTokenGenerationSuccessHandler;
import org.springframework.security.web.authentication.ott.RedirectOneTimeTokenGenerationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@AllArgsConstructor
public class CustomOTTGenerationSuccessHandler implements OneTimeTokenGenerationSuccessHandler {

   private final MailService mailService;

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            OneTimeToken ott) throws IOException {


        Map<String,Object> values= new HashMap<>();

        values.put("ott",ott.getTokenValue());
        values.put("expireAt",ott.getExpiresAt());

        String formattedBody = mailService.createFormattedBody("mail/send-ott", values);

        EmailDetails details = new EmailDetails(
                ott.getUsername(),
                formattedBody,
                "One-Time Token Verification");

        mailService.sendMail(details);

        new RedirectOneTimeTokenGenerationSuccessHandler("/verify-ott")
                .handle(request, response, ott);
    }
}
