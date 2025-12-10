package com.tpanh.backend.mapper;

import com.tpanh.backend.dto.RoomResponse;
import com.tpanh.backend.entity.Building;
import com.tpanh.backend.entity.Room;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-10T22:34:46+0700",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.44.0.v20251118-1623, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class RoomMapperImpl implements RoomMapper {

    @Override
    public RoomResponse toResponse(Room room) {
        if ( room == null ) {
            return null;
        }

        RoomResponse roomResponse = new RoomResponse();

        roomResponse.setBuildingId( roomBuildingId( room ) );
        roomResponse.setBuildingName( roomBuildingName( room ) );
        roomResponse.setId( room.getId() );
        roomResponse.setRoomNo( room.getRoomNo() );
        roomResponse.setPrice( room.getPrice() );
        roomResponse.setStatus( room.getStatus() );

        return roomResponse;
    }

    private Integer roomBuildingId(Room room) {
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

    private String roomBuildingName(Room room) {
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
}
