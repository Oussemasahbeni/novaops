package com.novaops.userservice.infrastructure.adapter.persistence;

import com.novaops.userservice.domain.enums.Locale;
import com.novaops.userservice.domain.enums.RoleType;
import com.novaops.userservice.domain.model.User;
import com.novaops.userservice.domain.port.output.UserRepository;
import com.novaops.userservice.exception.ConflictException;
import com.novaops.userservice.exception.NotFoundException;
import com.novaops.userservice.infrastructure.adapter.specifications.UserSpecifications;
import com.novaops.userservice.infrastructure.dto.request.UpdateUserRequest;
import com.novaops.userservice.infrastructure.entity.UserEntity;
import com.novaops.userservice.infrastructure.mapper.UserMapper;
import com.novaops.userservice.infrastructure.repository.UserJpaRepository;
import com.novaops.userservice.shared.annotation.PersistenceAdapter;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.hibernate.StaleObjectStateException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.annotation.Transactional;

@PersistenceAdapter
@RequiredArgsConstructor
public class UserJpaAdapter implements UserRepository {

  private final UserJpaRepository userRepository;
  private final UserMapper userMapper;

  @Override
  @Transactional
  public User create(User user) {
    UserEntity userEntity = userMapper.toUserEntity(user);
    UserEntity savedUser = userRepository.save(userEntity);
    return userMapper.toUser(savedUser);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<User> findById(UUID id) {
    UserEntity userEntity =
        userRepository
            .findById(id)
            .orElseThrow(
                () ->
                    new NotFoundException(NotFoundException.NotFoundExceptionType.USER_NOT_FOUND));

    User user = userMapper.toUser(userEntity);
    return Optional.of(user);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<User> findByEmail(String email) {
    return userRepository.findByEmail(email).map(userMapper::toUser);
  }

  @Override
  public void deleteById(UUID id) {
    userRepository.deleteById(id);
  }

  @Override
  @Transactional
  public User update(UpdateUserRequest updateUserRequest) {
    try {
      UserEntity existingUser =
          userRepository
              .findById(updateUserRequest.id())
              .orElseThrow(
                  () ->
                      new NotFoundException(
                          NotFoundException.NotFoundExceptionType.USER_NOT_FOUND));
      UserEntity user = userMapper.partialUpdate(updateUserRequest, existingUser);
      var savedUser = userRepository.save(user);
      return userMapper.toUser(savedUser);
    } catch (ObjectOptimisticLockingFailureException | StaleObjectStateException ex) {
      throw new ConflictException(ConflictException.ConflictExceptionType.CONFLICT_LOCK_VERSION);
    }
  }

  @Override
  public User update(User user) {
    try {
      UserEntity entity = userMapper.toUserEntity(user);
      var savedUser = userRepository.save(entity);
      return userMapper.toUser(savedUser);
    } catch (ObjectOptimisticLockingFailureException | StaleObjectStateException ex) {
      throw new ConflictException(ConflictException.ConflictExceptionType.CONFLICT_LOCK_VERSION);
    }
  }

  @Override
  public void updateLocale(String id, Locale locale) {
    UserEntity userEntity =
        userRepository
            .findById(UUID.fromString(id))
            .orElseThrow(
                () ->
                    new NotFoundException(NotFoundException.NotFoundExceptionType.USER_NOT_FOUND));
    userEntity.setLocale(locale);
    userRepository.save(userEntity);
  }

  @Override
  public Boolean existsByEmail(String email) {
    return userRepository.existsByEmail(email);
  }

  @Override
  public Page<User> findAll(String search, Pageable pageable, RoleType role) {
    return userRepository
        .findAll(UserSpecifications.hasCriteria(search, role), pageable)
        .map(userMapper::toUser);
  }
}
