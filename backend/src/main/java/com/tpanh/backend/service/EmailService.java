package com.tpanh.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    private static final String ACTIVATION_EMAIL_SUBJECT = "Kích hoạt tài khoản Manager";
    private static final String ACTIVATION_EMAIL_TEMPLATE =
            """
            Xin chào %s,

            Cảm ơn bạn đã đăng ký tài khoản Manager trên hệ thống quản lý phòng trọ.

            Vui lòng click vào link sau để kích hoạt tài khoản:
            %s

            Link này sẽ hết hạn sau 24 giờ.

            Trân trọng,
            Hệ thống quản lý phòng trọ
            """;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    public void sendActivationEmail(final String email, final String fullName, final String token) {
        final var activationLink = frontendUrl + "/activate?token=" + token;
        final var emailBody = String.format(ACTIVATION_EMAIL_TEMPLATE, fullName, activationLink);

        // TODO: Tích hợp với email service thực tế (SendGrid, AWS SES, etc.)
        // Hiện tại chỉ log để test
        log.info("Sending activation email to: {}", email);
        log.debug("Activation link: {}", activationLink);
        log.debug("Email body:\n{}", emailBody);

        // Trong production, sử dụng:
        // emailSender.send(email, ACTIVATION_EMAIL_SUBJECT, emailBody);
    }
}
