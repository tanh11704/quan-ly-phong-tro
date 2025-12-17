package com.tpanh.backend.dto;

import com.tpanh.backend.enums.RoomStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
@Schema(description = "Yêu cầu cập nhật phòng")
public class RoomUpdateRequest {
    @Schema(description = "Số phòng", example = "P.101")
    private String roomNo;

    @Min(value = 0, message = "PRICE_INVALID")
    @Schema(description = "Giá thuê (VNĐ/tháng)", example = "3000000")
    private Integer price;

    @Schema(
            description = "Trạng thái phòng",
            example = "OCCUPIED",
            allowableValues = {"VACANT", "OCCUPIED", "MAINTENANCE"})
    private RoomStatus status;
}
