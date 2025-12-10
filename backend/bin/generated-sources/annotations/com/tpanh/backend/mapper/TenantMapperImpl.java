package com.tpanh.backend.mapper;

import com.tpanh.backend.dto.TenantResponse;
import com.tpanh.backend.entity.Room;
import com.tpanh.backend.entity.Tenant;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-10T22:41:29+0700",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.44.0.v20251118-1623, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class TenantMapperImpl implements TenantMapper {

    @Override
    public TenantResponse toResponse(Tenant tenant) {
        if ( tenant == null ) {
            return null;
        }

        TenantResponse.TenantResponseBuilder tenantResponse = TenantResponse.builder();

        tenantResponse.roomId( tenantRoomId( tenant ) );
        tenantResponse.roomNo( tenantRoomRoomNo( tenant ) );
        tenantResponse.endDate( tenant.getEndDate() );
        tenantResponse.id( tenant.getId() );
        tenantResponse.isContractHolder( tenant.getIsContractHolder() );
        tenantResponse.name( tenant.getName() );
        tenantResponse.phone( tenant.getPhone() );
        tenantResponse.startDate( tenant.getStartDate() );

        return tenantResponse.build();
    }

    private Integer tenantRoomId(Tenant tenant) {
        if ( tenant == null ) {
            return null;
        }
        Room room = tenant.getRoom();
        if ( room == null ) {
            return null;
        }
        Integer id = room.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String tenantRoomRoomNo(Tenant tenant) {
        if ( tenant == null ) {
            return null;
        }
        Room room = tenant.getRoom();
        if ( room == null ) {
            return null;
        }
        String roomNo = room.getRoomNo();
        if ( roomNo == null ) {
            return null;
        }
        return roomNo;
    }
}
