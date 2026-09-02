package com.traazu.auth_service.mappers;

import org.mapstruct.Mapper;

import com.traazu.auth_service.domain.dtos.BaseUserProfileDto;
import com.traazu.auth_service.domain.entities.BaseUser;

@Mapper(componentModel = "spring")
public interface BaseUserMapper {

    BaseUserProfileDto toProfileDto(BaseUser baseUser);
    
}
