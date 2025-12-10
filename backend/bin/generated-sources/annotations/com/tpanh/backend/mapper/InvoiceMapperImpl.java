package com.tpanh.backend.mapper;

import com.tpanh.backend.dto.InvoiceResponse;
import com.tpanh.backend.entity.Invoice;
import com.tpanh.backend.entity.Room;
import com.tpanh.backend.entity.Tenant;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-10T18:50:50+0700",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.44.0.v20251118-1623, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class InvoiceMapperImpl implements InvoiceMapper {

    @Override
    public InvoiceResponse toResponse(Invoice invoice) {
        if ( invoice == null ) {
            return null;
        }

        InvoiceResponse invoiceResponse = new InvoiceResponse();

        invoiceResponse.setRoomNo( invoiceRoomRoomNo( invoice ) );
        invoiceResponse.setTenantName( invoiceTenantName( invoice ) );
        invoiceResponse.setDueDate( invoice.getDueDate() );
        invoiceResponse.setElecAmount( invoice.getElecAmount() );
        invoiceResponse.setId( invoice.getId() );
        invoiceResponse.setPeriod( invoice.getPeriod() );
        invoiceResponse.setRoomPrice( invoice.getRoomPrice() );
        invoiceResponse.setStatus( invoice.getStatus() );
        invoiceResponse.setTotalAmount( invoice.getTotalAmount() );
        invoiceResponse.setWaterAmount( invoice.getWaterAmount() );

        return invoiceResponse;
    }

    private String invoiceRoomRoomNo(Invoice invoice) {
        if ( invoice == null ) {
            return null;
        }
        Room room = invoice.getRoom();
        if ( room == null ) {
            return null;
        }
        String roomNo = room.getRoomNo();
        if ( roomNo == null ) {
            return null;
        }
        return roomNo;
    }

    private String invoiceTenantName(Invoice invoice) {
        if ( invoice == null ) {
            return null;
        }
        Tenant tenant = invoice.getTenant();
        if ( tenant == null ) {
            return null;
        }
        String name = tenant.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }
}
