package com.tpanh.backend.scheduler;

import com.tpanh.backend.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InvoiceScheduler {

    private final InvoiceService invoiceService;

    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Ho_Chi_Minh")
    public void markOverdueInvoices() {
        log.info("Starting scheduled job: markOverdueInvoices");
        try {
            final int count = invoiceService.markOverdueInvoices();
            log.info("Marked {} invoices as OVERDUE", count);
        } catch (final Exception e) {
            log.error("Failed to mark overdue invoices", e);
        }
    }
}
