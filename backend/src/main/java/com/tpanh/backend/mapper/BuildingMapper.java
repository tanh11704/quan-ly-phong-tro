package com.tpanh.backend.mapper;

import com.tpanh.backend.dto.BuildingResponse;
import com.tpanh.backend.entity.Building;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BuildingMapper {

    BuildingResponse toResponse(Building building);
}
