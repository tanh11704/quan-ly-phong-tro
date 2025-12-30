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
    INVOICE_NOT_FOUND(2004, "Không tìm thấy hóa đơn"),
    INVOICE_ALREADY_PAID(2005, "Hóa đơn đã được thanh toán"),
    INVOICE_CANNOT_BE_PAID(2006, "Hóa đơn không thể thanh toán ở trạng thái hiện tại"),
    TENANT_NOT_FOUND(2007, "Không tìm thấy khách thuê"),
    TENANT_CONTRACT_ALREADY_ENDED(2008, "Hợp đồng đã được kết thúc trước đó"),
    UTILITY_READING_NOT_FOUND(2009, "Không tìm thấy bản ghi chỉ số điện nước"),
    UTILITY_READING_EXISTED(2010, "Chỉ số điện nước cho phòng này trong tháng đã tồn tại"),
    UTILITY_READING_INVALID_INDEX(2011, "Chỉ số mới phải lớn hơn hoặc bằng chỉ số cũ"),
    MISSING_PREVIOUS_UTILITY_READING(
            2012,
            "Thiếu chỉ số điện/nước tháng trước. Vui lòng kiểm tra và nhập bổ sung trước khi tạo hóa đơn"),
    CONTRACT_HOLDER_ALREADY_EXISTS(2013, "Phòng đã có người đại diện hợp đồng đang hoạt động"),

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
    FORBIDDEN(3011, "Bạn không có quyền truy cập tài nguyên này"),
    USERNAME_ALREADY_EXISTS(3012, "Tên đăng nhập đã tồn tại"),
    EMAIL_ALREADY_EXISTS(3013, "Email đã được sử dụng"),
    EMAIL_REQUIRED(3014, "Email không được để trống"),
    EMAIL_INVALID(3015, "Email không hợp lệ"),
    FULL_NAME_REQUIRED(3016, "Họ và tên không được để trống"),
    USERNAME_INVALID_LENGTH(3017, "Tên đăng nhập phải từ 3 đến 50 ký tự"),
    PASSWORD_INVALID_LENGTH(3018, "Mật khẩu phải từ 6 đến 100 ký tự"),
    FULL_NAME_INVALID_LENGTH(3019, "Họ và tên không được vượt quá 100 ký tự"),
    USER_ALREADY_ACTIVE(3020, "Tài khoản đã được kích hoạt"),
    USER_PENDING_ACTIVATION(3021, "Tài khoản chưa được kích hoạt. Vui lòng kiểm tra email."),
    ;

    ErrorCode(final int code, final String message) {
        this.code = code;
        this.message = message;
    }

    private final int code;
    private final String message;
}
