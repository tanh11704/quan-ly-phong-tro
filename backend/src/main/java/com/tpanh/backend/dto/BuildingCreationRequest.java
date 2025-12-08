package com.tpanh.backend.dto;

import com.tpanh.backend.enums.WaterCalcMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Yêu cầu tạo tòa nhà mới")
public class BuildingCreationRequest {
    @NotBlank(message = "BUILDING_NAME_REQUIRED")
    @Schema(description = "Tên tòa nhà", example = "Trọ Xanh")
    private String name;

    @Schema(description = "Tên chủ tòa nhà", example = "Nguyễn Văn Chủ")
    private String ownerName;

    @Schema(description = "Số điện thoại chủ tòa nhà", example = "0909123456")
    private String ownerPhone;

    @Min(value = 0, message = "ELEC_PRICE_INVALID")
    @Schema(description = "Đơn giá điện (VNĐ/kWh)", example = "3500")
    private Integer elecUnitPrice;

    @Min(value = 0, message = "WATER_PRICE_INVALID")
    @Schema(description = "Đơn giá nước (VNĐ/m3 hoặc VNĐ/người)", example = "20000")
    private Integer waterUnitPrice;

    @NotNull(message = "WATER_CALC_METHOD_REQUIRED")
    @Schema(description = "Phương pháp tính nước", example = "BY_METER")
    private WaterCalcMethod waterCalcMethod;
}
