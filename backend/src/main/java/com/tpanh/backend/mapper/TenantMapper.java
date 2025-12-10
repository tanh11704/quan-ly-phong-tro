package com.tpanh.backend.mapper;

import com.tpanh.backend.dto.TenantResponse;
import com.tpanh.backend.entity.Tenant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TenantMapper {

    @Mapping(source = "room.id", target = "roomId")
    @Mapping(source = "room.roomNo", target = "roomNo")
    TenantResponse toResponse(Tenant tenant);
}
