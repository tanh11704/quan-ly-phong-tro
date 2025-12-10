package com.tpanh.backend.mapper;

import com.tpanh.backend.dto.BuildingResponse;
import com.tpanh.backend.entity.Building;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-10T22:41:29+0700",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.44.0.v20251118-1623, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class BuildingMapperImpl implements BuildingMapper {

    @Override
    public BuildingResponse toResponse(Building building) {
        if ( building == null ) {
            return null;
        }

        BuildingResponse.BuildingResponseBuilder buildingResponse = BuildingResponse.builder();

        buildingResponse.elecUnitPrice( building.getElecUnitPrice() );
        buildingResponse.id( building.getId() );
        buildingResponse.name( building.getName() );
        buildingResponse.ownerName( building.getOwnerName() );
        buildingResponse.ownerPhone( building.getOwnerPhone() );
        buildingResponse.waterCalcMethod( building.getWaterCalcMethod() );
        buildingResponse.waterUnitPrice( building.getWaterUnitPrice() );

        return buildingResponse.build();
    }
}
