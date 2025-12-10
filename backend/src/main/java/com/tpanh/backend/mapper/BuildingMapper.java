package com.tpanh.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.tpanh.backend.dto.BuildingResponse;
import com.tpanh.backend.entity.Building;

@Mapper(componentModel = "spring")
public interface BuildingMapper {

    @Mapping(source = "manager.id", target = "managerId")
    BuildingResponse toResponse(Building building);
}
