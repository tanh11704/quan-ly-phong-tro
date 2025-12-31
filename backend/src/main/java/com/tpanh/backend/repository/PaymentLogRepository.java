package com.tpanh.backend.repository;

import com.tpanh.backend.entity.PaymentLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentLogRepository extends JpaRepository<PaymentLog, Integer> {

    List<PaymentLog> findByInvoiceIdOrderByCreatedAtDesc(Integer invoiceId);
}
