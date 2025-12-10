package com.tpanh.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Response có phân trang")
public class PageResponse<T> {
    private static final int DEFAULT_SUCCESS_CODE = 1000;

    @Builder.Default
    @Schema(description = "Mã trả về", example = "1000")
    private int code = DEFAULT_SUCCESS_CODE;

    @Schema(description = "Thông báo", example = "Lấy danh sách thành công")
    private String message;

    @Schema(description = "Danh sách dữ liệu")
    private List<T> content;

    @Schema(description = "Thông tin phân trang")
    private PageInfo page;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Thông tin phân trang")
    public static class PageInfo {
        @Schema(description = "Số trang hiện tại (bắt đầu từ 0)", example = "0")
        private int page;

        @Schema(description = "Kích thước trang", example = "20")
        private int size;

        @Schema(description = "Tổng số phần tử", example = "100")
        private long totalElements;

        @Schema(description = "Tổng số trang", example = "5")
        private int totalPages;

        @Schema(description = "Có phải trang đầu tiên không", example = "true")
        private boolean first;

        @Schema(description = "Có phải trang cuối cùng không", example = "false")
        private boolean last;
    }
}
