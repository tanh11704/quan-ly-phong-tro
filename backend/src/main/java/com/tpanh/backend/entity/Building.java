package com.tpanh.backend.entity;

import com.tpanh.backend.enums.WaterCalcMethod;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "buildings")
@Data
public class Building {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name; // Tên tòa nhà (VD: Trọ Xanh)

    @Column(name = "owner_name")
    private String ownerName;

    @Column(name = "owner_phone")
    private String ownerPhone;

    // Cấu hình giá điện nước
    @Column(name = "elec_unit_price")
    private Integer elecUnitPrice;

    @Column(name = "water_unit_price")
    private Integer waterUnitPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "water_calc_method")
    private WaterCalcMethod waterCalcMethod;
}
