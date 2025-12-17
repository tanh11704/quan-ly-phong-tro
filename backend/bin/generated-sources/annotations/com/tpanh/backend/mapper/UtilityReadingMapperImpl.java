package com.tpanh.backend.mapper;

import com.tpanh.backend.dto.UtilityReadingResponse;
import com.tpanh.backend.entity.Room;
import com.tpanh.backend.entity.UtilityReading;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-17T20:13:22+0700",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.44.0.v20251118-1623, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class UtilityReadingMapperImpl implements UtilityReadingMapper {

    @Override
    public UtilityReadingResponse toResponse(UtilityReading utilityReading) {
        if ( utilityReading == null ) {
            return null;
        }

        UtilityReadingResponse utilityReadingResponse = new UtilityReadingResponse();

        utilityReadingResponse.setRoomId( utilityReadingRoomId( utilityReading ) );
        utilityReadingResponse.setRoomNo( utilityReadingRoomRoomNo( utilityReading ) );
        utilityReadingResponse.setId( utilityReading.getId() );
        utilityReadingResponse.setMonth( utilityReading.getMonth() );
        utilityReadingResponse.setElectricIndex( utilityReading.getElectricIndex() );
        utilityReadingResponse.setWaterIndex( utilityReading.getWaterIndex() );
        utilityReadingResponse.setImageEvidence( utilityReading.getImageEvidence() );
        utilityReadingResponse.setCreatedAt( utilityReading.getCreatedAt() );

        return utilityReadingResponse;
    }

    private Integer utilityReadingRoomId(UtilityReading utilityReading) {
        if ( utilityReading == null ) {
            return null;
        }
        Room room = utilityReading.getRoom();
        if ( room == null ) {
            return null;
        }
        Integer id = room.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String utilityReadingRoomRoomNo(UtilityReading utilityReading) {
        if ( utilityReading == null ) {
            return null;
        }
        Room room = utilityReading.getRoom();
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
