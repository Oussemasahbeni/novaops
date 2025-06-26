package com.novaops.userservice.domain.model;

import static org.springframework.util.StringUtils.capitalize;

import com.novaops.userservice.domain.enums.Gender;
import com.novaops.userservice.domain.enums.Locale;
import com.novaops.userservice.domain.enums.NotificationPreference;
import com.novaops.userservice.domain.enums.RoleType;
import com.novaops.userservice.shared.AbstractAuditingModel;
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
public class User extends AbstractAuditingModel {
  private UUID id;
  private String firstName;
  private String lastName;
  private String fullName;
  private String email;
  private String phoneNumber;
  private String profilePicture;
  private Gender gender;
  private String address;
  private LocalDate birthDate;
  private Locale locale;
  private NotificationPreference notificationPreference;
  private List<Role> roles = new ArrayList<>();

  public String getFullName() {
    return capitalize(firstName) + " " + capitalize(lastName);
  }

  public List<Role> setRolesFromRoleType(List<RoleType> roleTypes) {
    List<Role> userRoles = new ArrayList<>();
    for (RoleType roleType : roleTypes) {
      Role role = new Role();
      role.setName(roleType);
      role.setUser(this);
      userRoles.add(role);
    }
    return userRoles;
  }
}
