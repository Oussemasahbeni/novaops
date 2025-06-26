package com.novaops.userservice.infrastructure.adapter;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.novaops.userservice.domain.model.User;
import com.novaops.userservice.infrastructure.adapter.persistence.UserJpaAdapter;
import com.novaops.userservice.infrastructure.mapper.UserMapper;
import com.novaops.userservice.infrastructure.mapper.UserMapperImpl;
import com.novaops.userservice.infrastructure.repository.UserJpaRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase
@Import(UserMapperImpl.class)
public class UserJpaAdapterTest {

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgreSQLContainer =
      new PostgreSQLContainer<>("postgres:17-alpine");

  @Autowired private UserJpaRepository userJpaRepository;

  @Autowired private UserMapper userMapper;

  private UserJpaAdapter userJpaAdapter;

  @Test
  void connectionEstablished() {
    assertThat(postgreSQLContainer.isCreated()).isTrue();
    assertThat(postgreSQLContainer.isRunning()).isTrue();
  }

  @BeforeEach
  void setUp() {
    userJpaAdapter = new UserJpaAdapter(userJpaRepository, userMapper);
    userJpaRepository.deleteAll();
  }

  @Test
  void testCreateAndFindById() {
    User user =
        User.builder()
            .id(UUID.randomUUID())
            .email("test@example.com")
            .firstName("John")
            .lastName("Doe")
            .build();

    User savedUser = userJpaAdapter.create(user);

    assertThat(savedUser.getId()).isNotNull();
    assertThat(savedUser.getEmail()).isEqualTo("test@example.com");

    // Find by id
    Optional<User> foundUser = userJpaAdapter.findById(savedUser.getId());

    assertThat(foundUser).isPresent();
    assertThat(foundUser.get().getEmail()).isEqualTo("test@example.com");
  }

  @Test
  void testFindAllWithPaging() {
    // Create 2 users with different roles
    User user1 =
        User.builder()
            .id(UUID.randomUUID())
            .email("admin@example.com")
            .firstName("Admin")
            .lastName("One")
            .build();

    User user2 =
        User.builder()
            .id(UUID.randomUUID())
            .email("user@example.com")
            .firstName("User")
            .lastName("Two")
            .build();

    userJpaAdapter.create(user1);
    userJpaAdapter.create(user2);

    var page = userJpaAdapter.findAll("", PageRequest.of(0, 10), null);

    assertThat(page.getTotalElements()).isEqualTo(2);
    assertThat(page.getContent().getFirst().getEmail()).isEqualTo("admin@example.com");
  }
}
