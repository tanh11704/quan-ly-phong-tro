package com.tpanh.backend.mapper;

import com.tpanh.backend.dto.UserDTO;
import com.tpanh.backend.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDTO toDTO(User user);
}
