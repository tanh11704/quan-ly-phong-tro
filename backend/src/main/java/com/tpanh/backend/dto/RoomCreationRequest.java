package com.tpanh.backend.dto;

import com.tpanh.backend.enums.RoomStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Yêu cầu tạo phòng mới")
public class RoomCreationRequest {
    @NotNull(message = "BUILDING_ID_REQUIRED")
    @Schema(description = "ID tòa nhà", example = "1")
    private Integer buildingId;

    @NotBlank(message = "ROOM_NO_REQUIRED")
    @Schema(description = "Số phòng", example = "P.101")
    private String roomNo;

    @NotNull(message = "PRICE_REQUIRED")
    @Min(value = 0, message = "PRICE_INVALID")
    @Schema(description = "Giá thuê (VNĐ/tháng)", example = "3000000")
    private Integer price;

    @Schema(description = "Trạng thái phòng", example = "VACANT", allowableValues = {"VACANT", "OCCUPIED", "MAINTENANCE"})
    private RoomStatus status;
}
