package com.novaops.userservice.infrastructure.mapper;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

import com.novaops.userservice.domain.enums.RoleType;
import com.novaops.userservice.domain.model.Role;
import com.novaops.userservice.domain.model.User;
import com.novaops.userservice.domain.model.UserRegistrationData;
import com.novaops.userservice.infrastructure.dto.request.CsvUserRecord;
import com.novaops.userservice.infrastructure.dto.request.UpdateUserRequest;
import com.novaops.userservice.infrastructure.dto.request.UserRequestDto;
import com.novaops.userservice.infrastructure.dto.response.UserDto;
import com.novaops.userservice.infrastructure.entity.RoleEntity;
import com.novaops.userservice.infrastructure.entity.UserEntity;
import com.novaops.userservice.shared.mapstruct.CycleAvoidingMappingContext;
import com.novaops.userservice.shared.mapstruct.DoIgnore;
import java.util.List;
import org.mapstruct.*;

@Mapper(
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    componentModel = MappingConstants.ComponentModel.SPRING,
    nullValuePropertyMappingStrategy = IGNORE)
public interface UserMapper {

  UserEntity mapToUserEntity(
      User user, @Context CycleAvoidingMappingContext cycleAvoidingMappingContext);

  User mapToUser(
      UserEntity userEntity, @Context CycleAvoidingMappingContext cycleAvoidingMappingContext);

  @DoIgnore
  default UserEntity toUserEntity(User user) {
    UserEntity entity = mapToUserEntity(user, new CycleAvoidingMappingContext());

    if (entity.getRoles() != null) {
      for (RoleEntity role : entity.getRoles()) {
        role.setUser(entity);
      }
    }
    return entity;
  }

  @DoIgnore
  default User toUser(UserEntity userEntity) {
    return mapToUser(userEntity, new CycleAvoidingMappingContext());
  }

  User fromCsvRecord(CsvUserRecord csvUserRecord);

  UserRegistrationData fromRequestDto(UserRequestDto userRequestDto);

  UserDto toUserDto(User user);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  UserEntity partialUpdate(UpdateUserRequest userRequest, @MappingTarget UserEntity userEntity);

  default List<Role> map(RoleType roleType) {
    if (roleType == null) return null;

    Role role = Role.builder().name(roleType).build();

    return List.of(role);
  }
}
