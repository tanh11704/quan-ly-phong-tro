package com.tpanh.backend.entity;

import com.tpanh.backend.enums.InvoiceStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Entity
@Table(name = "invoices")
@Data
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Quan hệ Many-to-One: Nhiều hóa đơn thuộc về 1 Phòng
    @ManyToOne
    @JoinColumn(name = "room_id") // Tên cột khóa ngoại trong DB
    private Room room;

    // Quan hệ Many-to-One: Nhiều hóa đơn thuộc về 1 Khách (người trả tiền)
    @ManyToOne
    @JoinColumn(name = "tenant_id")
    private Tenant tenant;

    private String period; // Kỳ thanh toán: "2025-12"

    @Column(name = "room_price")
    private Integer roomPrice;

    @Column(name = "elec_amount")
    private Integer elecAmount;

    @Column(name = "water_amount")
    private Integer waterAmount;

    @Column(name = "total_amount")
    private Integer totalAmount;

    // Mapping Enum vào Database
    @Enumerated(EnumType.STRING) // Lưu dưới dạng chuỗi ("UNPAID") thay vì số (0,1)
    private InvoiceStatus status;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist // Tự động gán ngày tạo trước khi lưu vào DB
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = InvoiceStatus.DRAFT;
        }
    }
}
