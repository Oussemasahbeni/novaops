package com.novaops.userservice.infrastructure.repository;

import com.novaops.userservice.infrastructure.entity.UserEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserJpaRepository
    extends JpaRepository<UserEntity, UUID>, JpaSpecificationExecutor<UserEntity> {
  Optional<UserEntity> findByEmail(String email);

  Boolean existsByEmail(String email);

  @Query("SELECT u.email FROM UserEntity u")
  List<String> findAllEmails();
}
