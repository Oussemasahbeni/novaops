package com.novaops.userservice.domain.model;

import com.novaops.userservice.domain.enums.RoleType;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthUser {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private List<RoleType> roleTypes;
    private Boolean enabled;
    private Boolean emailVerified;
    private Instant createdAt;
    private Map<String, List<String>> attributes;

    public String getFullName() {
        return this.firstName + " " + this.lastName;
    }
}
