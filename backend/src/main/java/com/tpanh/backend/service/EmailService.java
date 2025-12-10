package com.tpanh.backend.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    private static final String ACTIVATION_EMAIL_SUBJECT = "Kích hoạt tài khoản Manager";
    private static final String EMAIL_TEMPLATE_PATH = "templates/activation-email.html";

    private final JavaMailSender mailSender;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    public void sendActivationEmail(
            final String email, final String fullName, final String token) {
        try {
            final var activationLink = frontendUrl + "/activate?token=" + token;
            final var htmlContent = loadEmailTemplate(fullName, activationLink);

            final MimeMessage message = mailSender.createMimeMessage();
            final MimeMessageHelper helper =
                    new MimeMessageHelper(
                            message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());

            helper.setFrom(fromEmail.isEmpty() ? "noreply@phongtro.com" : fromEmail);
            helper.setTo(email);
            helper.setSubject(ACTIVATION_EMAIL_SUBJECT);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Activation email sent successfully to: {}", email);
        } catch (final MessagingException | IOException e) {
            log.error("Failed to send activation email to: {}", email, e);
            throw new RuntimeException("Failed to send activation email", e);
        }
    }

    private String loadEmailTemplate(final String fullName, final String activationLink)
            throws IOException {
        final var resource = new ClassPathResource(EMAIL_TEMPLATE_PATH);
        final var templateContent =
                StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);

        return templateContent
                .replace("{{fullName}}", fullName)
                .replace("{{activationLink}}", activationLink);
    }
}
