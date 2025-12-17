package com.tpanh.backend.dto;

import com.tpanh.backend.enums.WaterCalcMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin tòa nhà")
public class BuildingResponse {
    @Schema(description = "ID tòa nhà", example = "1")
    private Integer id;

    @Schema(description = "Tên tòa nhà", example = "Trọ Xanh")
    private String name;

    @Schema(description = "Tên chủ tòa nhà", example = "Nguyễn Văn Chủ")
    private String ownerName;

    @Schema(description = "Số điện thoại chủ tòa nhà", example = "0909123456")
    private String ownerPhone;

    @Schema(description = "Đơn giá điện (VNĐ/kWh)", example = "3500")
    private Integer elecUnitPrice;

    @Schema(description = "Đơn giá nước (VNĐ/m3 hoặc VNĐ/người)", example = "20000")
    private Integer waterUnitPrice;

    @Schema(description = "Phương pháp tính nước", example = "BY_METER")
    private WaterCalcMethod waterCalcMethod;

    @Schema(
            description = "ID của Manager quản lý tòa nhà",
            example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    private String managerId;
}
