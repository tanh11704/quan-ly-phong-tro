package com.tpanh.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tpanh.backend.entity.Invoice;

public interface InvoiceRepository extends JpaRepository<Invoice, Integer> {

}
