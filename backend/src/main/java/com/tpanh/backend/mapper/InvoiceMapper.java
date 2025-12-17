package com.tpanh.backend.mapper;

import com.tpanh.backend.dto.InvoiceDetailResponse;
import com.tpanh.backend.dto.InvoiceResponse;
import com.tpanh.backend.entity.Invoice;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InvoiceMapper {

    @Mapping(source = "room.roomNo", target = "roomNo")
    @Mapping(source = "tenant.name", target = "tenantName")
    InvoiceResponse toResponse(Invoice invoice);

    @Mapping(source = "room.roomNo", target = "roomNo")
    @Mapping(source = "room.id", target = "roomId")
    @Mapping(source = "room.building.name", target = "buildingName")
    @Mapping(source = "room.building.id", target = "buildingId")
    @Mapping(source = "tenant.name", target = "tenantName")
    @Mapping(source = "tenant.phone", target = "tenantPhone")
    @Mapping(source = "tenant.id", target = "tenantId")
    @Mapping(target = "elecPreviousValue", ignore = true)
    @Mapping(target = "elecCurrentValue", ignore = true)
    @Mapping(target = "elecUsage", ignore = true)
    @Mapping(target = "elecUnitPrice", ignore = true)
    @Mapping(target = "waterPreviousValue", ignore = true)
    @Mapping(target = "waterCurrentValue", ignore = true)
    @Mapping(target = "waterUsage", ignore = true)
    @Mapping(target = "waterUnitPrice", ignore = true)
    InvoiceDetailResponse toDetailResponse(Invoice invoice);
}
