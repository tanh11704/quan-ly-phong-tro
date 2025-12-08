package com.tpanh.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin phòng")
public class RoomResponse {
    @Schema(description = "ID phòng", example = "1")
    private Integer id;

    @Schema(description = "ID tòa nhà", example = "1")
    private Integer buildingId;

    @Schema(description = "Tên tòa nhà", example = "Trọ Xanh")
    private String buildingName;

    @Schema(description = "Số phòng", example = "P.101")
    private String roomNo;

    @Schema(description = "Giá thuê (VNĐ/tháng)", example = "3000000")
    private Integer price;

    @Schema(description = "Trạng thái phòng", example = "OCCUPIED")
    private String status;
}
