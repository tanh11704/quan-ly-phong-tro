package com.tpanh.backend.controller;

import com.tpanh.backend.dto.ApiResponse;
import com.tpanh.backend.dto.InvoiceCreationRequest;
import com.tpanh.backend.dto.InvoiceResponse;
import com.tpanh.backend.entity.Building;
import com.tpanh.backend.exception.AppException;
import com.tpanh.backend.exception.ErrorCode;
import com.tpanh.backend.repository.BuildingRepository;
import com.tpanh.backend.service.InvoiceService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${app.api-prefix}/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final BuildingRepository buildingRepository;

    @PostMapping("/generate")
    @PreAuthorize("hasRole('MANAGER')")
    public ApiResponse<List<InvoiceResponse>> createInvoices(
            @RequestBody @Valid final InvoiceCreationRequest request) {

        final Building building =
                buildingRepository
                        .findById(request.getBuildingId())
                        .orElseThrow(() -> new AppException(ErrorCode.BUILDING_NOT_FOUND));

        final List<InvoiceResponse> result =
                invoiceService.createInvoice(building, request.getPeriod());

        return ApiResponse.<List<InvoiceResponse>>builder()
                .result(result)
                .message("Tạo hóa đơn thành công")
                .build();
    }
}
