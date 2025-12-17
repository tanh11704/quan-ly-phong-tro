package com.tpanh.backend.mapper;

import com.tpanh.backend.dto.InvoiceDetailResponse;
import com.tpanh.backend.dto.InvoiceResponse;
import com.tpanh.backend.entity.Building;
import com.tpanh.backend.entity.Invoice;
import com.tpanh.backend.entity.Room;
import com.tpanh.backend.entity.Tenant;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-17T20:11:45+0700",
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
        invoiceResponse.setId( invoice.getId() );
        invoiceResponse.setPeriod( invoice.getPeriod() );
        invoiceResponse.setRoomPrice( invoice.getRoomPrice() );
        invoiceResponse.setElecAmount( invoice.getElecAmount() );
        invoiceResponse.setWaterAmount( invoice.getWaterAmount() );
        invoiceResponse.setTotalAmount( invoice.getTotalAmount() );
        invoiceResponse.setStatus( invoice.getStatus() );
        invoiceResponse.setDueDate( invoice.getDueDate() );

        return invoiceResponse;
    }

    @Override
    public InvoiceDetailResponse toDetailResponse(Invoice invoice) {
        if ( invoice == null ) {
            return null;
        }

        InvoiceDetailResponse invoiceDetailResponse = new InvoiceDetailResponse();

        invoiceDetailResponse.setRoomNo( invoiceRoomRoomNo( invoice ) );
        invoiceDetailResponse.setRoomId( invoiceRoomId( invoice ) );
        invoiceDetailResponse.setBuildingName( invoiceRoomBuildingName( invoice ) );
        invoiceDetailResponse.setBuildingId( invoiceRoomBuildingId( invoice ) );
        invoiceDetailResponse.setTenantName( invoiceTenantName( invoice ) );
        invoiceDetailResponse.setTenantPhone( invoiceTenantPhone( invoice ) );
        invoiceDetailResponse.setTenantId( invoiceTenantId( invoice ) );
        invoiceDetailResponse.setCreatedAt( invoice.getCreatedAt() );
        invoiceDetailResponse.setDueDate( invoice.getDueDate() );
        invoiceDetailResponse.setElecAmount( invoice.getElecAmount() );
        invoiceDetailResponse.setId( invoice.getId() );
        invoiceDetailResponse.setPaidAt( invoice.getPaidAt() );
        invoiceDetailResponse.setPeriod( invoice.getPeriod() );
        invoiceDetailResponse.setRoomPrice( invoice.getRoomPrice() );
        invoiceDetailResponse.setStatus( invoice.getStatus() );
        invoiceDetailResponse.setTotalAmount( invoice.getTotalAmount() );
        invoiceDetailResponse.setWaterAmount( invoice.getWaterAmount() );

        return invoiceDetailResponse;
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

    private Integer invoiceRoomId(Invoice invoice) {
        if ( invoice == null ) {
            return null;
        }
        Room room = invoice.getRoom();
        if ( room == null ) {
            return null;
        }
        Integer id = room.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String invoiceRoomBuildingName(Invoice invoice) {
        if ( invoice == null ) {
            return null;
        }
        Room room = invoice.getRoom();
        if ( room == null ) {
            return null;
        }
        Building building = room.getBuilding();
        if ( building == null ) {
            return null;
        }
        String name = building.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }

    private Integer invoiceRoomBuildingId(Invoice invoice) {
        if ( invoice == null ) {
            return null;
        }
        Room room = invoice.getRoom();
        if ( room == null ) {
            return null;
        }
        Building building = room.getBuilding();
        if ( building == null ) {
            return null;
        }
        Integer id = building.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String invoiceTenantPhone(Invoice invoice) {
        if ( invoice == null ) {
            return null;
        }
        Tenant tenant = invoice.getTenant();
        if ( tenant == null ) {
            return null;
        }
        String phone = tenant.getPhone();
        if ( phone == null ) {
            return null;
        }
        return phone;
    }

    private Integer invoiceTenantId(Invoice invoice) {
        if ( invoice == null ) {
            return null;
        }
        Tenant tenant = invoice.getTenant();
        if ( tenant == null ) {
            return null;
        }
        Integer id = tenant.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
