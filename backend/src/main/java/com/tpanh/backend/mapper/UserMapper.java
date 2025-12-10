package com.tpanh.backend.mapper;

import com.tpanh.backend.dto.UserDTO;
import com.tpanh.backend.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "roles", target = "role")
    UserDTO toDTO(User user);
}
