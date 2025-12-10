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
    BUILDING_NAME_REQUIRED(1004, "Tên tòa nhà không được để trống"),
    WATER_CALC_METHOD_REQUIRED(1005, "Phương pháp tính nước không được để trống"),
    ELEC_PRICE_INVALID(1006, "Đơn giá điện không hợp lệ"),
    WATER_PRICE_INVALID(1007, "Đơn giá nước không hợp lệ"),
    BUILDING_ID_REQUIRED(1008, "ID tòa nhà không được để trống"),
    ROOM_NO_REQUIRED(1009, "Số phòng không được để trống"),
    PRICE_REQUIRED(1010, "Giá thuê không được để trống"),
    PRICE_INVALID(1011, "Giá thuê không hợp lệ"),
    ROOM_ID_REQUIRED(1012, "ID phòng không được để trống"),
    TENANT_NAME_REQUIRED(1013, "Tên khách thuê không được để trống"),

    // Nhóm lỗi Nghiệp vụ (20xx) - Business Logic
    BUILDING_NOT_FOUND(2001, "Không tìm thấy tòa nhà này trong hệ thống"),
    ROOM_NOT_FOUND(2002, "Không tìm thấy phòng trọ"),
    INVOICE_EXISTED(2003, "Hóa đơn cho phòng này trong tháng đã tồn tại"),
    TENANT_NOT_FOUND(2004, "Không tìm thấy khách thuê"),
    TENANT_CONTRACT_ALREADY_ENDED(2005, "Hợp đồng đã được kết thúc trước đó"),

    // Nhóm lỗi Xác thực (30xx) - Authentication
    USERNAME_REQUIRED(3001, "Tên đăng nhập không được để trống"),
    PASSWORD_REQUIRED(3002, "Mật khẩu không được để trống"),
    TOKEN_REQUIRED(3003, "Token không được để trống"),
    ZALO_TOKEN_REQUIRED(3004, "Token Zalo không được để trống"),
    INVALID_CREDENTIALS(3005, "Tên đăng nhập hoặc mật khẩu không đúng"),
    USER_NOT_FOUND(3006, "Không tìm thấy người dùng"),
    USER_INACTIVE(3007, "Tài khoản đã bị vô hiệu hóa"),
    ZALO_AUTH_FAILED(3008, "Xác thực với Zalo thất bại"),
    INVALID_TOKEN(3009, "Token không hợp lệ hoặc đã hết hạn"),
    UNAUTHORIZED(3010, "Người dùng chưa đăng nhập hoặc không có quyền truy cập"),
    ;

    ErrorCode(final int code, final String message) {
        this.code = code;
        this.message = message;
    }

    private final int code;
    private final String message;
}
