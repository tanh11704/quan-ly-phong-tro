package com.tpanh.backend.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Properties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock private JavaMailSender mailSender;

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailService(mailSender);
        ReflectionTestUtils.setField(emailService, "frontendUrl", "http://localhost:5173");
        ReflectionTestUtils.setField(emailService, "fromEmail", "");
    }

    @Test
    void sendInvoiceEmail_withBlankEmail_shouldNotSend() {
        assertDoesNotThrow(
                () ->
                        emailService.sendInvoiceEmail(
                                "  ",
                                "Nguyễn Văn A",
                                "P.101",
                                "2025-01",
                                100_000,
                                java.time.LocalDate.of(2025, 1, 10)));
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void sendInvoiceEmail_withValidEmail_shouldSend() throws Exception {
        final var session = Session.getDefaultInstance(new Properties());
        final var mimeMessage = new MimeMessage(session);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendInvoiceEmail(
                "tenant@example.com",
                "Nguyễn Văn A",
                "P.101",
                "2025-01",
                123_456,
                java.time.LocalDate.of(2025, 1, 10));

        final var captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(captor.capture());

        final MimeMessage sent = captor.getValue();
        assertEquals("Hóa đơn phòng P.101 - Tháng 2025-01", sent.getSubject());
        assertNotNull(sent.getFrom());
        assertEquals("noreply@phongtro.com", ((InternetAddress) sent.getFrom()[0]).getAddress());

        final var recipients = sent.getRecipients(Message.RecipientType.TO);
        assertNotNull(recipients);
        assertEquals(1, recipients.length);
        assertEquals("tenant@example.com", ((InternetAddress) recipients[0]).getAddress());
    }

    @Test
    void sendInvoiceEmail_whenMessagingException_shouldThrowRuntimeException() throws Exception {
        // Use an invalid email to deterministically trigger MessagingException (AddressException)
        final var session = Session.getDefaultInstance(new Properties());
        final var mimeMessage = new MimeMessage(session);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        final var ex =
                assertThrows(
                        RuntimeException.class,
                        () ->
                                emailService.sendInvoiceEmail(
                                        "invalid@@example.com",
                                        "Nguyễn Văn A",
                                        "P.101",
                                        "2025-01",
                                        100_000,
                                        java.time.LocalDate.of(2025, 1, 10)));
        assertNotNull(ex.getMessage());
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void sendActivationEmail_withValidEmail_shouldSend() throws Exception {
        final var session = Session.getDefaultInstance(new Properties());
        final var mimeMessage = new MimeMessage(session);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendActivationEmail("admin@example.com", "Test Admin", "token-123");

        final var captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(captor.capture());

        final MimeMessage sent = captor.getValue();
        assertEquals("Kích hoạt tài khoản Manager", sent.getSubject());
        assertNotNull(sent.getRecipients(Message.RecipientType.TO));
    }

    // ===== Additional tests for better branch coverage =====

    @Test
    void sendInvoiceEmail_withNullEmail_shouldNotSend() {
        assertDoesNotThrow(
                () ->
                        emailService.sendInvoiceEmail(
                                null,
                                "Nguyễn Văn A",
                                "P.101",
                                "2025-01",
                                100_000,
                                java.time.LocalDate.of(2025, 1, 10)));
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void sendInvoiceEmail_withConfiguredFromEmail_shouldUseConfiguredEmail() throws Exception {
        // Set non-empty fromEmail
        ReflectionTestUtils.setField(emailService, "fromEmail", "custom@example.com");

        final var session = Session.getDefaultInstance(new Properties());
        final var mimeMessage = new MimeMessage(session);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendInvoiceEmail(
                "tenant@example.com",
                "Nguyễn Văn A",
                "P.101",
                "2025-01",
                500_000,
                java.time.LocalDate.of(2025, 1, 15));

        final var captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(captor.capture());

        final MimeMessage sent = captor.getValue();
        assertEquals("custom@example.com", ((InternetAddress) sent.getFrom()[0]).getAddress());
    }

    @Test
    void sendActivationEmail_withConfiguredFromEmail_shouldUseConfiguredEmail() throws Exception {
        // Set non-empty fromEmail
        ReflectionTestUtils.setField(emailService, "fromEmail", "sender@example.com");

        final var session = Session.getDefaultInstance(new Properties());
        final var mimeMessage = new MimeMessage(session);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendActivationEmail("admin@example.com", "Test Admin", "token-456");

        final var captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(captor.capture());

        final MimeMessage sent = captor.getValue();
        assertEquals("sender@example.com", ((InternetAddress) sent.getFrom()[0]).getAddress());
    }

    @Test
    void sendActivationEmail_whenMessagingException_shouldThrowRuntimeException() throws Exception {
        // Use an invalid email format to trigger MessagingException
        final var session = Session.getDefaultInstance(new Properties());
        final var mimeMessage = new MimeMessage(session);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        final var ex =
                assertThrows(
                        RuntimeException.class,
                        () ->
                                emailService.sendActivationEmail(
                                        "invalid@@email.com",
                                        "Test Admin",
                                        "token-123"));
        assertNotNull(ex.getMessage());
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void sendInvoiceEmail_withLargeAmount_shouldFormatCorrectly() throws Exception {
        final var session = Session.getDefaultInstance(new Properties());
        final var mimeMessage = new MimeMessage(session);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendInvoiceEmail(
                "tenant@example.com",
                "Nguyễn Văn B",
                "P.202",
                "2025-06",
                12_345_678,
                java.time.LocalDate.of(2025, 6, 20));

        final var captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(captor.capture());

        final MimeMessage sent = captor.getValue();
        assertEquals("Hóa đơn phòng P.202 - Tháng 2025-06", sent.getSubject());
    }
}

