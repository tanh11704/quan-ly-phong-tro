package com.tpanh.backend.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    // Nhóm lỗi hệ thống (99xx)
    UNCATEGORIZED_EXCEPTION(9999, "Lỗi hệ thống chưa được định nghĩa"),

    // Nhóm lỗi Validation (10xx)
    INVALID_KEY(1001, "Mã lỗi không hợp lệ"),
    INVALID_BUILDING_ID(1002, "ID tòa nhà không được để trống"),
    INVALID_PERIOD(1003, "Kỳ thanh toán không được để trống"),

    // Nhóm lỗi Nghiệp vụ (20xx) - Business Logic
    BUILDING_NOT_FOUND(2001, "Không tìm thấy tòa nhà này trong hệ thống"),
    ROOM_NOT_FOUND(2002, "Không tìm thấy phòng trọ"),
    INVOICE_EXISTED(2003, "Hóa đơn cho phòng này trong tháng đã tồn tại"),
    ;

    ErrorCode(final int code, final String message) {
        this.code = code;
        this.message = message;
    }

    private final int code;
    private final String message;
}
