package com.novaops.userservice.infrastructure.entity;

import com.novaops.userservice.domain.enums.Gender;
import com.novaops.userservice.domain.enums.Locale;
import com.novaops.userservice.shared.AbstractAuditingEntity;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "users")
public class UserEntity extends AbstractAuditingEntity {

  @Id private UUID id;

  @Column(name = "first_name", nullable = false)
  private String firstName;

  @Column(name = "last_name", nullable = false)
  private String lastName;

  @Column(name = "email", nullable = false, unique = true)
  private String email;

  @Column(name = "phone_number")
  private String phoneNumber;

  @Column(name = "profile_picture")
  private String profilePicture;

  @Enumerated(EnumType.STRING)
  @Column(name = "locale")
  private Locale locale;

  @Column(name = "birth_date")
  private LocalDate birthDate;

  @Column(name = "address")
  private String address;

  @Enumerated(EnumType.STRING)
  @Column(name = "gender")
  private Gender gender;

  @OneToMany(
      mappedBy = "user",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  private List<RoleEntity> roles = new ArrayList<>();

  @PrePersist
  @PreUpdate
  public void setEmailToLowerCase() {
    this.email = this.email.toLowerCase();
  }
}
