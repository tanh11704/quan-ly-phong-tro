package com.tpanh.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin khách thuê")
public class TenantResponse {
    @Schema(description = "ID khách thuê", example = "1")
    private Integer id;

    @Schema(description = "ID phòng", example = "1")
    private Integer roomId;

    @Schema(description = "Số phòng", example = "P.101")
    private String roomNo;

    @Schema(description = "Tên khách thuê", example = "Nguyễn Văn A")
    private String name;

    @Schema(description = "Số điện thoại", example = "0901234567")
    private String phone;

    @Schema(description = "Có phải người đại diện hợp đồng không", example = "true")
    private Boolean isContractHolder;

    @Schema(description = "Ngày bắt đầu hợp đồng", example = "2025-01-01")
    private LocalDate startDate;

    @Schema(description = "Ngày kết thúc hợp đồng", example = "2025-12-31")
    private LocalDate endDate;
}
