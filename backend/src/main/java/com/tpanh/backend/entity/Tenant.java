package com.tpanh.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Data;

@Entity
@Table(name = "tenants")
@Data
public class Tenant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Quan hệ: Nhiều khách ở 1 phòng
    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    @Column(name = "user_id")
    private String userId;

    private String name;
    private String phone;
    private String email;

    @Column(name = "is_contract_holder")
    private Boolean isContractHolder; // Người đại diện

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "contract_end_date")
    private LocalDate contractEndDate; // Ngày hết hạn hợp đồng dự kiến (null = vô thời hạn)

    @Column(name = "end_date")
    private LocalDate endDate; // Ngày thực sự dọn đi (set khi gọi endTenantContract)
}
