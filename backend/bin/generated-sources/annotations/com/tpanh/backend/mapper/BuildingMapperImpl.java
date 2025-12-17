package com.tpanh.backend.mapper;

import com.tpanh.backend.dto.BuildingResponse;
import com.tpanh.backend.entity.Building;
import com.tpanh.backend.entity.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-17T20:11:45+0700",
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

        buildingResponse.managerId( buildingManagerId( building ) );
        buildingResponse.elecUnitPrice( building.getElecUnitPrice() );
        buildingResponse.id( building.getId() );
        buildingResponse.name( building.getName() );
        buildingResponse.ownerName( building.getOwnerName() );
        buildingResponse.ownerPhone( building.getOwnerPhone() );
        buildingResponse.waterCalcMethod( building.getWaterCalcMethod() );
        buildingResponse.waterUnitPrice( building.getWaterUnitPrice() );

        return buildingResponse.build();
    }

    private String buildingManagerId(Building building) {
        if ( building == null ) {
            return null;
        }
        User manager = building.getManager();
        if ( manager == null ) {
            return null;
        }
        String id = manager.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
