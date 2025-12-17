package com.tpanh.backend.controller;

import com.tpanh.backend.config.PaginationConfig;
import com.tpanh.backend.dto.ApiResponse;
import com.tpanh.backend.dto.InvoiceCreationRequest;
import com.tpanh.backend.dto.InvoiceDetailResponse;
import com.tpanh.backend.dto.InvoiceResponse;
import com.tpanh.backend.dto.PageResponse;
import com.tpanh.backend.entity.Building;
import com.tpanh.backend.enums.InvoiceStatus;
import com.tpanh.backend.exception.AppException;
import com.tpanh.backend.exception.ErrorCode;
import com.tpanh.backend.repository.BuildingRepository;
import com.tpanh.backend.service.InvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${app.api-prefix}/invoices")
@RequiredArgsConstructor
@Tag(name = "Invoices", description = "API quản lý hóa đơn (dành cho Manager)")
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final BuildingRepository buildingRepository;

    @Operation(
            summary = "Tạo hóa đơn cho tòa nhà",
            description =
                    "Tạo hóa đơn tự động cho tất cả phòng có khách thuê trong tòa nhà theo kỳ thanh toán. "
                            + "Hóa đơn sẽ tính toán: tiền phòng, tiền điện (theo chỉ số công tơ), tiền nước (theo phương pháp đã cấu hình). "
                            + "Các phòng đã có hóa đơn trong kỳ này sẽ được bỏ qua.")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Tạo hóa đơn thành công"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "400",
                        description = "Dữ liệu không hợp lệ"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "403",
                        description = "Không có quyền truy cập"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "404",
                        description = "Không tìm thấy tòa nhà")
            })
    @PostMapping("/generate")
    @PreAuthorize("hasRole('MANAGER')")
    public ApiResponse<List<InvoiceResponse>> createInvoices(
            @RequestBody @Valid final InvoiceCreationRequest request) {
        final var authentication =
                org.springframework.security.core.context.SecurityContextHolder.getContext()
                        .getAuthentication();
        final var currentUserId = authentication.getName();

        final Building building =
                buildingRepository
                        .findByIdAndManagerId(request.getBuildingId(), currentUserId)
                        .orElseThrow(() -> new AppException(ErrorCode.BUILDING_NOT_FOUND));

        final List<InvoiceResponse> result =
                invoiceService.createInvoice(building, request.getPeriod());

        return ApiResponse.<List<InvoiceResponse>>builder()
                .result(result)
                .message("Tạo hóa đơn thành công")
                .build();
    }

    @Operation(
            summary = "Danh sách hóa đơn",
            description =
                    "Lấy danh sách hóa đơn của tòa nhà (có phân trang). "
                            + "Có thể lọc theo period (kỳ thanh toán) và status (trạng thái). "
                            + "Sử dụng query parameters: page, size, sort, period, status")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Lấy danh sách hóa đơn thành công"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "403",
                        description = "Không có quyền truy cập"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "404",
                        description = "Không tìm thấy tòa nhà")
            })
    @GetMapping
    @PreAuthorize("hasRole('MANAGER')")
    public PageResponse<InvoiceResponse> getInvoices(
            @Parameter(description = "ID tòa nhà", required = true) @RequestParam
                    final Integer buildingId,
            @Parameter(description = "Kỳ thanh toán (VD: 2025-01)", example = "2025-01")
                    @RequestParam(required = false)
                    final String period,
            @Parameter(description = "Trạng thái hóa đơn", example = "UNPAID")
                    @RequestParam(required = false)
                    final InvoiceStatus status,
            @Parameter(description = "Thông tin phân trang (page, size, sort)")
                    @PageableDefault(
                            size = PaginationConfig.DEFAULT_PAGE_SIZE,
                            sort = "createdAt",
                            direction = Sort.Direction.DESC)
                    final Pageable pageable) {
        final var authentication = SecurityContextHolder.getContext().getAuthentication();
        final var currentUserId = authentication.getName();

        // Verify building belongs to current manager
        buildingRepository
                .findByIdAndManagerId(buildingId, currentUserId)
                .orElseThrow(() -> new AppException(ErrorCode.BUILDING_NOT_FOUND));

        return invoiceService.getInvoices(buildingId, period, status, pageable);
    }

    @Operation(
            summary = "Chi tiết hóa đơn",
            description =
                    "Lấy thông tin chi tiết hóa đơn bao gồm: số điện cũ/mới, số nước, các thành phần tiền. "
                            + "Chỉ Manager sở hữu tòa nhà hoặc Admin mới có quyền xem.")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Lấy chi tiết hóa đơn thành công"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "403",
                        description = "Không có quyền truy cập"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "404",
                        description = "Không tìm thấy hóa đơn")
            })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ApiResponse<InvoiceDetailResponse> getInvoiceDetail(@PathVariable final Integer id) {
        final var response = invoiceService.getInvoiceDetail(id);
        return ApiResponse.<InvoiceDetailResponse>builder()
                .result(response)
                .message("Lấy chi tiết hóa đơn thành công")
                .build();
    }

    @Operation(
            summary = "Thanh toán hóa đơn",
            description =
                    "Xác nhận khách đã đóng tiền. Chuyển trạng thái từ UNPAID/DRAFT sang PAID. "
                            + "Chỉ Manager sở hữu tòa nhà mới có quyền thực hiện.")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Thanh toán hóa đơn thành công"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "400",
                        description = "Hóa đơn đã được thanh toán hoặc không thể thanh toán"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "403",
                        description = "Không có quyền truy cập"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "404",
                        description = "Không tìm thấy hóa đơn")
            })
    @PutMapping("/{id}/pay")
    @PreAuthorize("hasRole('MANAGER')")
    public ApiResponse<InvoiceResponse> payInvoice(@PathVariable final Integer id) {
        final var response = invoiceService.payInvoice(id);
        return ApiResponse.<InvoiceResponse>builder()
                .result(response)
                .message("Thanh toán hóa đơn thành công")
                .build();
    }

    @Operation(
            summary = "Gửi email hóa đơn",
            description =
                    "Gửi email thông báo hóa đơn cho khách thuê. "
                            + "Email sẽ chứa thông tin: số phòng, kỳ thanh toán, tổng tiền, hạn thanh toán.")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Gửi email thành công"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "400",
                        description = "Khách thuê chưa có email"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "403",
                        description = "Không có quyền truy cập"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "404",
                        description = "Không tìm thấy hóa đơn")
            })
    @PostMapping("/{id}/send-email")
    @PreAuthorize("hasRole('MANAGER')")
    public ApiResponse<String> sendInvoiceEmail(@PathVariable final Integer id) {
        invoiceService.sendInvoiceEmail(id);
        return ApiResponse.<String>builder()
                .result("Email đã được gửi thành công")
                .message("Hóa đơn đã được gửi đến email của khách thuê")
                .build();
    }
}
