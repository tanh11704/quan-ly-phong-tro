package com.tpanh.backend.repository;

import com.tpanh.backend.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceRepository extends JpaRepository<Invoice, Integer> {

    boolean existsByRoomIdAndPeriod(Integer roomId, String period);
}
