package com.tpanh.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "rooms")
@Data
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Quan hệ: Nhiều phòng thuộc 1 tòa nhà
    @ManyToOne
    @JoinColumn(name = "building_id")
    private Building building;

    @Column(name = "room_no")
    private String roomNo; // Số phòng (P.101)

    private Integer price; // Giá thuê

    private String status; // 'VACANT', 'OCCUPIED'...
}
