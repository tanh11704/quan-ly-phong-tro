package com.tpanh.backend.service;

import com.tpanh.backend.dto.InvoiceResponse;
import com.tpanh.backend.entity.Building;
import com.tpanh.backend.entity.Invoice;
import com.tpanh.backend.entity.Room;
import com.tpanh.backend.entity.Tenant;
import com.tpanh.backend.enums.InvoiceStatus;
import com.tpanh.backend.enums.MeterType;
import com.tpanh.backend.enums.WaterCalcMethod;
import com.tpanh.backend.mapper.InvoiceMapper;
import com.tpanh.backend.repository.InvoiceRepository;
import com.tpanh.backend.repository.MeterRecordRepository;
import com.tpanh.backend.repository.RoomRepository;
import com.tpanh.backend.repository.TenantRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
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

    @Transactional
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
        final var elecRecord =
                meterRecordRepository.findByRoomIdAndPeriodAndType(roomId, period, MeterType.ELEC);
        if (elecRecord.isPresent()) {
            final int usage =
                    elecRecord.get().getCurrentValue() - elecRecord.get().getPreviousValue();
            return usage * building.getElecUnitPrice();
        }
        return 0;
    }

    private int calculateWaterCost(
            final Integer roomId, final String period, final Building building) {
        final var waterRecord =
                meterRecordRepository.findByRoomIdAndPeriodAndType(roomId, period, MeterType.WATER);
        final int waterUsage =
                waterRecord.map(r -> r.getCurrentValue() - r.getPreviousValue()).orElse(0);
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
        if (building.getWaterCalcMethod() == null) {
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
}
