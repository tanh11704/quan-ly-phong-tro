package com.tpanh.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.tpanh.backend.dto.UtilityReadingResponse;
import com.tpanh.backend.entity.UtilityReading;

@Mapper(componentModel = "spring")
public interface UtilityReadingMapper {

    @Mapping(source = "room.id", target = "roomId")
    @Mapping(source = "room.roomNo", target = "roomNo")
    UtilityReadingResponse toResponse(UtilityReading utilityReading);
}
