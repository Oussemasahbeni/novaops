package com.novaops.userservice.infrastructure.mapper;

import com.novaops.userservice.domain.model.AuthUser;
import com.novaops.userservice.infrastructure.dto.request.UpdateUserRequest;
import com.novaops.userservice.infrastructure.dto.response.AuthUserDto;
import java.time.Instant;
import java.util.List;
import org.keycloak.representations.idm.UserRepresentation;
import org.mapstruct.*;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class AuthMapper {

  public abstract AuthUserDto toUserDto(AuthUser user);

  public abstract AuthUser toAuthUser(UpdateUserRequest userRequestDto);

  public abstract List<AuthUserDto> toUserDtoList(List<AuthUser> users);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  public abstract AuthUser partialUpdate(AuthUser user, @MappingTarget AuthUser authUser);

  public abstract UserRepresentation toUserRepresentation(AuthUser user);

  @Mapping(
      target = "createdAt",
      source = "createdTimestamp",
      qualifiedByName = "getCreatedTimeStamp")
  public abstract AuthUser toAuthUser(UserRepresentation userRepresentation);

  @Named("getCreatedTimeStamp")
  public Instant getCreatedTimeStamp(Long createdTimestamp) {
    return Instant.ofEpochMilli(createdTimestamp);
  }
}
