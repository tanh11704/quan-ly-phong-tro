package com.tpanh.backend.mapper;

import com.tpanh.backend.dto.InvoiceResponse;
import com.tpanh.backend.entity.Invoice;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InvoiceMapper {

    @Mapping(source = "room.roomNo", target = "roomNo")
    @Mapping(source = "tenant.name", target = "tenantName")
    InvoiceResponse toResponse(Invoice invoice);
}
