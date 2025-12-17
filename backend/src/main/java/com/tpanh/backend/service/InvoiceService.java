package com.tpanh.backend.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tpanh.backend.dto.InvoiceDetailResponse;
import com.tpanh.backend.dto.InvoiceResponse;
import com.tpanh.backend.dto.PageResponse;
import com.tpanh.backend.entity.Building;
import com.tpanh.backend.entity.Invoice;
import com.tpanh.backend.entity.MeterRecord;
import com.tpanh.backend.entity.Room;
import com.tpanh.backend.entity.Tenant;
import com.tpanh.backend.entity.UtilityReading;
import com.tpanh.backend.enums.InvoiceStatus;
import com.tpanh.backend.enums.MeterType;
import com.tpanh.backend.enums.WaterCalcMethod;
import com.tpanh.backend.exception.AppException;
import com.tpanh.backend.exception.ErrorCode;
import com.tpanh.backend.mapper.InvoiceMapper;
import com.tpanh.backend.repository.InvoiceRepository;
import com.tpanh.backend.repository.MeterRecordRepository;
import com.tpanh.backend.repository.RoomRepository;
import com.tpanh.backend.repository.TenantRepository;
import com.tpanh.backend.repository.UtilityReadingRepository;

import lombok.RequiredArgsConstructor;

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
    private final EmailService emailService;

    @Transactional
    @Caching(
            evict = {
                @CacheEvict(value = "rooms", key = "#building.id"),
                @CacheEvict(value = "tenantsByRoom", allEntries = true)
            })
    public List<InvoiceResponse> createInvoice(final Building building, final String period) {
        final List<InvoiceResponse> results = new ArrayList<>();
        final List<Room> rooms = roomRepository.findByBuildingId(building.getId());

        for (final Room room : rooms) {
            if (shouldSkipRoom(room.getId(), period)) {
                continue;
            }

            final Tenant tenant = findContractHolder(room.getId());
            if (tenant == null) {
                continue;
            }

            final int elecCost = calculateElectricityCost(room.getId(), period, building);
            final int waterCost = calculateWaterCost(room.getId(), period, building);

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
            final Integer roomId, final String period, final Building building) {
        // Try to use UtilityReading first (new approach)
        final var currentReading = utilityReadingRepository.findByRoomIdAndMonth(roomId, period);
        if (currentReading.isPresent()
                && currentReading.get().getElectricIndex() != null
                && building.getElecUnitPrice() != null) {
            final String previousMonth = getPreviousMonth(period);
            final var previousReading =
                    utilityReadingRepository.findByRoomIdAndMonth(roomId, previousMonth);

            final int currentValue = currentReading.get().getElectricIndex();
            final int previousValue =
                    previousReading.map(UtilityReading::getElectricIndex).orElse(0);

            final int usage = currentValue - previousValue;
            if (usage < 0) {
                return 0; // Guard clause: negative usage is invalid
            }
            return usage * building.getElecUnitPrice();
        }

        // Fallback to MeterRecord (backward compatibility)
        final var elecRecord =
                meterRecordRepository.findByRoomIdAndPeriodAndType(roomId, period, MeterType.ELEC);
        if (elecRecord.isPresent() && building.getElecUnitPrice() != null) {
            final int usage =
                    elecRecord.get().getCurrentValue() - elecRecord.get().getPreviousValue();
            if (usage < 0) {
                return 0; // Guard clause: negative usage is invalid
            }
            return usage * building.getElecUnitPrice();
        }
        return 0;
    }

    private int calculateWaterCost(
            final Integer roomId, final String period, final Building building) {
        // Try to use UtilityReading first (new approach)
        final var currentReading = utilityReadingRepository.findByRoomIdAndMonth(roomId, period);
        if (currentReading.isPresent() && currentReading.get().getWaterIndex() != null) {
            final String previousMonth = getPreviousMonth(period);
            final var previousReading =
                    utilityReadingRepository.findByRoomIdAndMonth(roomId, previousMonth);

            final int currentValue = currentReading.get().getWaterIndex();
            final int previousValue =
                    previousReading.map(UtilityReading::getWaterIndex).orElse(0);

            final int waterUsage = currentValue - previousValue;
            final int validUsage = waterUsage < 0 ? 0 : waterUsage; // Guard clause
            final int tenantCount = tenantRepository.countByRoomId(roomId);
            return calculateWaterFee(building, validUsage, tenantCount);
        }

        // Fallback to MeterRecord (backward compatibility)
        final var waterRecord =
                meterRecordRepository.findByRoomIdAndPeriodAndType(roomId, period, MeterType.WATER);
        final int waterUsage =
                waterRecord
                        .map(r -> {
                            final int usage = r.getCurrentValue() - r.getPreviousValue();
                            return usage < 0 ? 0 : usage; // Guard clause: negative usage is invalid
                        })
                        .orElse(0);
        final int tenantCount = tenantRepository.countByRoomId(roomId);
        return calculateWaterFee(building, waterUsage, tenantCount);
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

    @Cacheable(value = "invoices", key = "#id")
    public InvoiceDetailResponse getInvoiceDetail(final Integer id) {
        final Invoice invoice =
                invoiceRepository
                        .findById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_FOUND));

        final InvoiceDetailResponse response = invoiceMapper.toDetailResponse(invoice);

        // Try to use UtilityReading first (new approach)
        final var currentReading =
                utilityReadingRepository.findByRoomIdAndMonth(
                        invoice.getRoom().getId(), invoice.getPeriod());
        if (currentReading.isPresent()) {
            final String previousMonth = getPreviousMonth(invoice.getPeriod());
            final var previousReading =
                    utilityReadingRepository.findByRoomIdAndMonth(
                            invoice.getRoom().getId(), previousMonth);

            // Electricity
            if (currentReading.get().getElectricIndex() != null) {
                final int currentValue = currentReading.get().getElectricIndex();
                final int previousValue =
                        previousReading.map(UtilityReading::getElectricIndex).orElse(0);
                response.setElecPreviousValue(previousValue);
                response.setElecCurrentValue(currentValue);
                response.setElecUsage(currentValue - previousValue);
                if (invoice.getRoom().getBuilding().getElecUnitPrice() != null) {
                    response.setElecUnitPrice(invoice.getRoom().getBuilding().getElecUnitPrice());
                }
            }

            // Water
            if (currentReading.get().getWaterIndex() != null) {
                final int currentValue = currentReading.get().getWaterIndex();
                final int previousValue =
                        previousReading.map(UtilityReading::getWaterIndex).orElse(0);
                response.setWaterPreviousValue(previousValue);
                response.setWaterCurrentValue(currentValue);
                response.setWaterUsage(currentValue - previousValue);
                if (invoice.getRoom().getBuilding().getWaterUnitPrice() != null) {
                    response.setWaterUnitPrice(invoice.getRoom().getBuilding().getWaterUnitPrice());
                }
            }

            return response;
        }

        // Fallback to MeterRecord (backward compatibility)
        final Optional<MeterRecord> elecRecord =
                meterRecordRepository.findByRoomIdAndPeriodAndType(
                        invoice.getRoom().getId(), invoice.getPeriod(), MeterType.ELEC);
        if (elecRecord.isPresent()) {
            final MeterRecord record = elecRecord.get();
            response.setElecPreviousValue(record.getPreviousValue());
            response.setElecCurrentValue(record.getCurrentValue());
            response.setElecUsage(record.getCurrentValue() - record.getPreviousValue());
            if (invoice.getRoom().getBuilding().getElecUnitPrice() != null) {
                response.setElecUnitPrice(invoice.getRoom().getBuilding().getElecUnitPrice());
            }
        }

        final Optional<MeterRecord> waterRecord =
                meterRecordRepository.findByRoomIdAndPeriodAndType(
                        invoice.getRoom().getId(), invoice.getPeriod(), MeterType.WATER);
        if (waterRecord.isPresent()) {
            final MeterRecord record = waterRecord.get();
            response.setWaterPreviousValue(record.getPreviousValue());
            response.setWaterCurrentValue(record.getCurrentValue());
            response.setWaterUsage(record.getCurrentValue() - record.getPreviousValue());
            if (invoice.getRoom().getBuilding().getWaterUnitPrice() != null) {
                response.setWaterUnitPrice(invoice.getRoom().getBuilding().getWaterUnitPrice());
            }
        }

        return response;
    }

    @Transactional
    @CacheEvict(value = "invoices", key = "#id")
    public InvoiceResponse payInvoice(final Integer id) {
        final Invoice invoice =
                invoiceRepository
                        .findById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_FOUND));

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new AppException(ErrorCode.INVOICE_ALREADY_PAID);
        }

        if (invoice.getStatus() != InvoiceStatus.UNPAID && invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new AppException(ErrorCode.INVOICE_CANNOT_BE_PAID);
        }

        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setPaidAt(java.time.LocalDateTime.now());
        final Invoice savedInvoice = invoiceRepository.save(invoice);

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

    private String getPreviousMonth(final String period) {
        // Format: "YYYY-MM" -> "YYYY-MM" (previous month)
        try {
            final var parts = period.split("-");
            final int year = Integer.parseInt(parts[0]);
            final int month = Integer.parseInt(parts[1]);

            int prevYear = year;
            int prevMonth = month - 1;

            if (prevMonth < 1) {
                prevMonth = 12;
                prevYear = year - 1;
            }

            return String.format("%04d-%02d", prevYear, prevMonth);
        } catch (final Exception e) {
            // If parsing fails, return empty string (will result in no previous reading)
            return "";
        }
    }
}
