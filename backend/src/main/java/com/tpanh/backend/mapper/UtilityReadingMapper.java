package com.tpanh.backend.mapper;

import com.tpanh.backend.dto.UtilityReadingResponse;
import com.tpanh.backend.entity.UtilityReading;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UtilityReadingMapper {

    @Mapping(source = "room.id", target = "roomId")
    @Mapping(source = "room.roomNo", target = "roomNo")
    UtilityReadingResponse toResponse(UtilityReading utilityReading);
}
