package com.tpanh.backend.dto;

import com.tpanh.backend.enums.WaterCalcMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Yêu cầu cập nhật tòa nhà")
public class BuildingUpdateRequest {
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
}
