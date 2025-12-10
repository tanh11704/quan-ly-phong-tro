package com.tpanh.backend.mapper;

import com.tpanh.backend.dto.RoomResponse;
import com.tpanh.backend.entity.Room;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoomMapper {

    @Mapping(source = "building.id", target = "buildingId")
    @Mapping(source = "building.name", target = "buildingName")
    RoomResponse toResponse(Room room);
}
