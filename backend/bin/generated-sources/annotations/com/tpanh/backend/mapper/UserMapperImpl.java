package com.tpanh.backend.mapper;

import com.tpanh.backend.dto.UserDTO;
import com.tpanh.backend.entity.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-19T08:55:55+0700",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.44.0.v20251118-1623, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserDTO toDTO(User user) {
        if ( user == null ) {
            return null;
        }

        UserDTO.UserDTOBuilder userDTO = UserDTO.builder();

        userDTO.role( user.getRoles() );
        userDTO.active( user.getActive() );
        userDTO.fullName( user.getFullName() );
        userDTO.id( user.getId() );
        userDTO.username( user.getUsername() );

        return userDTO.build();
    }
}
