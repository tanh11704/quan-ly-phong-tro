package com.tpanh.backend.service;

import com.tpanh.backend.dto.InvoiceDetailResponse;
import com.tpanh.backend.dto.InvoiceResponse;
import com.tpanh.backend.dto.PageResponse;
import com.tpanh.backend.entity.Building;
import com.tpanh.backend.entity.Invoice;
import com.tpanh.backend.entity.MeterRecord;
import com.tpanh.backend.entity.PaymentLog;
import com.tpanh.backend.entity.Room;
import com.tpanh.backend.entity.Tenant;
import com.tpanh.backend.entity.UtilityReading;
import com.tpanh.backend.enums.InvoiceStatus;
import com.tpanh.backend.enums.MeterType;
import com.tpanh.backend.enums.WaterCalcMethod;
import com.tpanh.backend.exception.AppException;
import com.tpanh.backend.exception.ErrorCode;
import com.tpanh.backend.mapper.InvoiceMapper;
import com.tpanh.backend.repository.BuildingRepository;
import com.tpanh.backend.repository.InvoiceRepository;
import com.tpanh.backend.repository.MeterRecordRepository;
import com.tpanh.backend.repository.PaymentLogRepository;
import com.tpanh.backend.repository.RoomRepository;
import com.tpanh.backend.repository.TenantRepository;
import com.tpanh.backend.repository.UtilityReadingRepository;
import com.tpanh.backend.security.CurrentUser;
import com.tpanh.backend.util.PeriodUtils;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private static final int DUE_DATE_DAYS = 5;

    private final InvoiceMapper invoiceMapper;
    private final InvoiceRepository invoiceRepository;
    private final RoomRepository roomRepository;
    private final TenantRepository tenantRepository;
    private final MeterRecordRepository meterRecordRepository;
    private final UtilityReadingRepository utilityReadingRepository;
    private final BuildingRepository buildingRepository;
    private final EmailService emailService;
    private final PaymentLogRepository paymentLogRepository;
    private final CurrentUser currentUser;

    @Transactional
    @PreAuthorize("@invoicePermission.canAccessBuildingInvoices(#buildingId, authentication)")
    public List<InvoiceResponse> createInvoicesForBuilding(
            final Integer buildingId, final String period) {
        final List<InvoiceResponse> results = new ArrayList<>();
        final List<Room> rooms = roomRepository.findByBuildingId(buildingId);

        for (final Room room : rooms) {
            if (shouldSkipRoom(room.getId(), period)) {
                continue;
            }

            final Tenant tenant = findContractHolder(room.getId());
            if (tenant == null) {
                continue;
            }

            final int elecCost = calculateElectricityCost(room.getId(), period, buildingId);
            final int waterCost = calculateWaterCost(room.getId(), period, buildingId);

            final Invoice invoice = buildInvoice(room, tenant, period, elecCost, waterCost);
            final Invoice savedInvoice = invoiceRepository.save(invoice);
            results.add(invoiceMapper.toResponse(savedInvoice));
        }

        return results;
    }

    private boolean shouldSkipRoom(final Integer roomId, final String period) {
        return invoiceRepository.existsByRoomIdAndPeriod(roomId, period);
    }

    private Tenant findContractHolder(final Integer roomId) {
        final var tenantOpt = tenantRepository.findByRoomIdAndIsContractHolderTrue(roomId);
        return tenantOpt.orElse(null);
    }

    private int calculateElectricityCost(
            final Integer roomId, final String period, final Integer buildingId) {
        final var building =
                buildingRepository
                        .findById(buildingId)
                        .orElseThrow(() -> new AppException(ErrorCode.BUILDING_NOT_FOUND));

        final Integer elecUnitPrice = building.getElecUnitPrice();
        if (elecUnitPrice == null) {
            return 0;
        }
        final var costFromUtilityReading =
                calculateElectricityCostFromUtilityReading(roomId, period, elecUnitPrice);
        if (costFromUtilityReading != null) {
            return costFromUtilityReading;
        }
        return calculateElectricityCostFromMeterRecord(roomId, period, elecUnitPrice);
    }

    private int calculateWaterCost(
            final Integer roomId, final String period, final Integer buildingId) {
        final var building =
                buildingRepository
                        .findById(buildingId)
                        .orElseThrow(() -> new AppException(ErrorCode.BUILDING_NOT_FOUND));
        final var costFromUtilityReading = calculateWaterUsageFromUtilityReading(roomId, period);
        if (costFromUtilityReading != null) {
            final int tenantCount = tenantRepository.countByRoomId(roomId);
            return calculateWaterFee(building, costFromUtilityReading, tenantCount);
        }
        final int waterUsageFromMeterRecord = calculateWaterUsageFromMeterRecord(roomId, period);
        final int tenantCount = tenantRepository.countByRoomId(roomId);
        return calculateWaterFee(building, waterUsageFromMeterRecord, tenantCount);
    }

    private Invoice buildInvoice(
            final Room room,
            final Tenant tenant,
            final String period,
            final int elecCost,
            final int waterCost) {
        final Invoice invoice = new Invoice();
        invoice.setRoom(room);
        invoice.setTenant(tenant);
        invoice.setPeriod(period);
        invoice.setRoomPrice(room.getPrice());
        invoice.setElecAmount(elecCost);
        invoice.setWaterAmount(waterCost);
        invoice.setTotalAmount(room.getPrice() + elecCost + waterCost);
        invoice.setStatus(InvoiceStatus.DRAFT);
        invoice.setDueDate(LocalDate.now().plusDays(DUE_DATE_DAYS));
        return invoice;
    }

    private Integer calculateWaterFee(
            final Building building, final Integer usage, final Integer tenantCount) {
        if (building.getWaterCalcMethod() == null || building.getWaterUnitPrice() == null) {
            return 0;
        }

        if (building.getWaterCalcMethod() == WaterCalcMethod.PER_CAPITA) {
            return building.getWaterUnitPrice() * tenantCount;
        }

        if (building.getWaterCalcMethod() == WaterCalcMethod.BY_METER) {
            return building.getWaterUnitPrice() * usage;
        }

        return 0;
    }

    @PreAuthorize("@invoicePermission.canAccessBuildingInvoices(#buildingId, authentication)")
    public PageResponse<InvoiceResponse> getInvoices(
            final Integer buildingId,
            final String period,
            final InvoiceStatus status,
            final Pageable pageable) {
        final Page<Invoice> invoicePage =
                invoiceRepository.findByBuildingIdWithFilters(buildingId, period, status, pageable);

        final var content =
                invoicePage.getContent().stream().map(invoiceMapper::toResponse).toList();

        return PageResponse.<InvoiceResponse>builder()
                .content(content)
                .page(buildPageInfo(invoicePage))
                .message("Lấy danh sách hóa đơn thành công")
                .build();
    }

    @PreAuthorize("@invoicePermission.canAccessInvoice(#id, authentication)")
    @Cacheable(value = "invoices", key = "#id")
    public InvoiceDetailResponse getInvoiceDetail(final Integer id) {
        final Invoice invoice = getInvoiceOrThrow(id);
        final InvoiceDetailResponse response = invoiceMapper.toDetailResponse(invoice);
        if (tryPopulateUtilityReadingDetails(invoice, response)) {
            return response;
        }
        populateMeterRecordDetails(invoice, response);
        return response;
    }

    private Invoice getInvoiceOrThrow(final Integer id) {
        return invoiceRepository
                .findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_FOUND));
    }

    private Integer calculateElectricityCostFromUtilityReading(
            final Integer roomId, final String period, final Integer elecUnitPrice) {
        final var currentReading = utilityReadingRepository.findByRoomIdAndMonth(roomId, period);
        if (currentReading.isEmpty() || currentReading.get().getElectricIndex() == null) {
            return null;
        }
        final var previousReading =
                utilityReadingRepository.findByRoomIdAndMonth(
                        roomId, PeriodUtils.getPreviousMonth(period));
        final int currentValue = currentReading.get().getElectricIndex();
        final int previousValue =
                resolvePreviousElectricIndexOrThrow(roomId, period, previousReading);
        final int usage = currentValue - previousValue;
        return usage < 0 ? 0 : usage * elecUnitPrice;
    }

    private int calculateElectricityCostFromMeterRecord(
            final Integer roomId, final String period, final Integer elecUnitPrice) {
        final var elecRecord =
                meterRecordRepository.findByRoomIdAndPeriodAndType(roomId, period, MeterType.ELEC);
        if (elecRecord.isEmpty()) {
            return 0;
        }
        final int usage = elecRecord.get().getCurrentValue() - elecRecord.get().getPreviousValue();
        return usage < 0 ? 0 : usage * elecUnitPrice;
    }

    private Integer calculateWaterUsageFromUtilityReading(
            final Integer roomId, final String period) {
        final var currentReading = utilityReadingRepository.findByRoomIdAndMonth(roomId, period);
        if (currentReading.isEmpty() || currentReading.get().getWaterIndex() == null) {
            return null;
        }
        final var previousReading =
                utilityReadingRepository.findByRoomIdAndMonth(
                        roomId, PeriodUtils.getPreviousMonth(period));
        final int currentValue = currentReading.get().getWaterIndex();
        final int previousValue = resolvePreviousWaterIndexOrThrow(roomId, period, previousReading);
        final int usage = currentValue - previousValue;
        return usage < 0 ? 0 : usage;
    }

    private int calculateWaterUsageFromMeterRecord(final Integer roomId, final String period) {
        final var waterRecord =
                meterRecordRepository.findByRoomIdAndPeriodAndType(roomId, period, MeterType.WATER);
        if (waterRecord.isEmpty()) {
            return 0;
        }
        final int usage =
                waterRecord.get().getCurrentValue() - waterRecord.get().getPreviousValue();
        return usage < 0 ? 0 : usage;
    }

    private boolean tryPopulateUtilityReadingDetails(
            final Invoice invoice, final InvoiceDetailResponse response) {
        final var currentReading =
                utilityReadingRepository.findByRoomIdAndMonth(
                        invoice.getRoom().getId(), invoice.getPeriod());
        if (currentReading.isEmpty()) {
            return false;
        }
        final var previousReading =
                utilityReadingRepository.findByRoomIdAndMonth(
                        invoice.getRoom().getId(),
                        PeriodUtils.getPreviousMonth(invoice.getPeriod()));
        populateElectricityFromUtilityReading(
                invoice, response, currentReading.get(), previousReading);
        populateWaterFromUtilityReading(invoice, response, currentReading.get(), previousReading);
        return true;
    }

    private void populateElectricityFromUtilityReading(
            final Invoice invoice,
            final InvoiceDetailResponse response,
            final UtilityReading currentReading,
            final Optional<UtilityReading> previousReading) {
        if (currentReading.getElectricIndex() == null) {
            return;
        }
        final int currentValue = currentReading.getElectricIndex();
        final Integer previousValue =
                resolvePreviousElectricIndexForDisplay(
                        invoice.getRoom().getId(), invoice.getPeriod(), previousReading);
        response.setElecPreviousValue(previousValue);
        response.setElecCurrentValue(currentValue);
        response.setElecUsage(previousValue != null ? currentValue - previousValue : null);
        response.setElecUnitPrice(invoice.getRoom().getBuilding().getElecUnitPrice());
    }

    private void populateWaterFromUtilityReading(
            final Invoice invoice,
            final InvoiceDetailResponse response,
            final UtilityReading currentReading,
            final Optional<UtilityReading> previousReading) {
        if (currentReading.getWaterIndex() == null) {
            return;
        }
        final int currentValue = currentReading.getWaterIndex();
        final Integer previousValue =
                resolvePreviousWaterIndexForDisplay(
                        invoice.getRoom().getId(), invoice.getPeriod(), previousReading);
        response.setWaterPreviousValue(previousValue);
        response.setWaterCurrentValue(currentValue);
        response.setWaterUsage(previousValue != null ? currentValue - previousValue : null);
        response.setWaterUnitPrice(invoice.getRoom().getBuilding().getWaterUnitPrice());
    }

    private void populateMeterRecordDetails(
            final Invoice invoice, final InvoiceDetailResponse response) {
        populateElectricityFromMeterRecord(invoice, response);
        populateWaterFromMeterRecord(invoice, response);
    }

    private void populateElectricityFromMeterRecord(
            final Invoice invoice, final InvoiceDetailResponse response) {
        final var elecRecord =
                meterRecordRepository.findByRoomIdAndPeriodAndType(
                        invoice.getRoom().getId(), invoice.getPeriod(), MeterType.ELEC);
        if (elecRecord.isEmpty()) {
            return;
        }
        final MeterRecord record = elecRecord.get();
        response.setElecPreviousValue(record.getPreviousValue());
        response.setElecCurrentValue(record.getCurrentValue());
        response.setElecUsage(record.getCurrentValue() - record.getPreviousValue());
        response.setElecUnitPrice(invoice.getRoom().getBuilding().getElecUnitPrice());
    }

    private void populateWaterFromMeterRecord(
            final Invoice invoice, final InvoiceDetailResponse response) {
        final var waterRecord =
                meterRecordRepository.findByRoomIdAndPeriodAndType(
                        invoice.getRoom().getId(), invoice.getPeriod(), MeterType.WATER);
        if (waterRecord.isEmpty()) {
            return;
        }
        final MeterRecord record = waterRecord.get();
        response.setWaterPreviousValue(record.getPreviousValue());
        response.setWaterCurrentValue(record.getCurrentValue());
        response.setWaterUsage(record.getCurrentValue() - record.getPreviousValue());
        response.setWaterUnitPrice(invoice.getRoom().getBuilding().getWaterUnitPrice());
    }

    private int resolvePreviousElectricIndexOrThrow(
            final Integer roomId,
            final String period,
            final Optional<UtilityReading> previousReading) {
        if (previousReading.isPresent() && previousReading.get().getElectricIndex() != null) {
            return previousReading.get().getElectricIndex();
        }

        final boolean hasHistoryBeforeCurrentMonth =
                utilityReadingRepository.existsByRoomIdAndMonthLessThan(roomId, period);
        if (hasHistoryBeforeCurrentMonth) {
            throw new AppException(ErrorCode.MISSING_PREVIOUS_UTILITY_READING);
        }
        return 0;
    }

    private int resolvePreviousWaterIndexOrThrow(
            final Integer roomId,
            final String period,
            final Optional<UtilityReading> previousReading) {
        if (previousReading.isPresent() && previousReading.get().getWaterIndex() != null) {
            return previousReading.get().getWaterIndex();
        }

        final boolean hasHistoryBeforeCurrentMonth =
                utilityReadingRepository.existsByRoomIdAndMonthLessThan(roomId, period);
        if (hasHistoryBeforeCurrentMonth) {
            throw new AppException(ErrorCode.MISSING_PREVIOUS_UTILITY_READING);
        }
        return 0;
    }

    private Integer resolvePreviousElectricIndexForDisplay(
            final Integer roomId,
            final String period,
            final Optional<UtilityReading> previousReading) {
        if (previousReading.isPresent() && previousReading.get().getElectricIndex() != null) {
            return previousReading.get().getElectricIndex();
        }
        final boolean hasHistoryBeforeCurrentMonth =
                utilityReadingRepository.existsByRoomIdAndMonthLessThan(roomId, period);
        return hasHistoryBeforeCurrentMonth ? null : 0;
    }

    private Integer resolvePreviousWaterIndexForDisplay(
            final Integer roomId,
            final String period,
            final Optional<UtilityReading> previousReading) {
        if (previousReading.isPresent() && previousReading.get().getWaterIndex() != null) {
            return previousReading.get().getWaterIndex();
        }
        final boolean hasHistoryBeforeCurrentMonth =
                utilityReadingRepository.existsByRoomIdAndMonthLessThan(roomId, period);
        return hasHistoryBeforeCurrentMonth ? null : 0;
    }

    @PreAuthorize("@invoicePermission.canAccessInvoice(#id, authentication)")
    @Transactional
    @CacheEvict(value = "invoices", key = "#p0")
    public InvoiceResponse payInvoice(final Integer id) {
        final Invoice invoice =
                invoiceRepository
                        .findById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_FOUND));

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new AppException(ErrorCode.INVOICE_ALREADY_PAID);
        }

        if (invoice.getStatus() != InvoiceStatus.UNPAID
                && invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new AppException(ErrorCode.INVOICE_CANNOT_BE_PAID);
        }

        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setPaidAt(java.time.LocalDateTime.now());
        final Invoice savedInvoice = invoiceRepository.save(invoice);

        paymentLogRepository.save(
                PaymentLog.create(
                        savedInvoice,
                        "PAID",
                        InvoiceStatus.UNPAID.name(),
                        InvoiceStatus.PAID.name(),
                        currentUser.getUserId(),
                        "Invoice paid manually"));

        return invoiceMapper.toResponse(savedInvoice);
    }

    private PageResponse.PageInfo buildPageInfo(final Page<?> page) {
        return PageResponse.PageInfo.builder()
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }

    @Transactional
    @PreAuthorize("@invoicePermission.canAccessInvoice(#id, authentication)")
    public void sendInvoiceEmail(final Integer invoiceId) {
        final Invoice invoice =
                invoiceRepository
                        .findById(invoiceId)
                        .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_FOUND));

        if (invoice.getTenant() == null) {
            throw new AppException(ErrorCode.TENANT_NOT_FOUND);
        }

        final String email = invoice.getTenant().getEmail();
        if (email == null || email.isBlank()) {
            throw new AppException(ErrorCode.EMAIL_REQUIRED);
        }

        emailService.sendInvoiceEmail(
                email,
                invoice.getTenant().getName(),
                invoice.getRoom().getRoomNo(),
                invoice.getPeriod(),
                invoice.getTotalAmount(),
                invoice.getDueDate());
    }

    @Transactional
    @CacheEvict(value = "invoices", allEntries = true)
    public int markOverdueInvoices() {
        final List<Invoice> overdueInvoices =
                invoiceRepository.findOverdueInvoices(LocalDate.now());
        if (overdueInvoices.isEmpty()) {
            return 0;
        }

        for (final Invoice invoice : overdueInvoices) {
            final String oldStatus = invoice.getStatus().name();
            invoice.setStatus(InvoiceStatus.OVERDUE);
            final Invoice savedInvoice = invoiceRepository.save(invoice);

            paymentLogRepository.save(
                    PaymentLog.create(
                            savedInvoice,
                            "MARKED_OVERDUE",
                            oldStatus,
                            InvoiceStatus.OVERDUE.name(),
                            "SYSTEM",
                            "Auto marked overdue by scheduler"));
        }

        return overdueInvoices.size();
    }
}
