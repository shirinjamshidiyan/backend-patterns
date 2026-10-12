package com.shirin.twofactorauthentication.service;

import com.shirin.twofactorauthentication.entity.EmailDetails;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import java.util.Map;

@Service
@AllArgsConstructor
public class MailService{


    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public void sendMail(EmailDetails details) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper= new MimeMessageHelper(message,true,"UTF-8");

            helper.setFrom("no-reply@mfa-demo.test");
            helper.setTo(details.recipient());
            helper.setSubject(details.subject());
            helper.setText(details.body(), true);
            mailSender.send(message);
        }
        catch (MessagingException ex)
        {
            throw new IllegalStateException("Failed to send email", ex);
        }
    }

    /*
    To use the HTML template in an email, you can create a String variable
     containing the content of the template and then use that variable to
     set the email’s content
     */

    public String createFormattedBody(
            String templateName,
            Map<String,Object> values) {

        Context context = new Context();
        context.setVariables(values);

        return templateEngine.process(templateName, context);

    }
}
