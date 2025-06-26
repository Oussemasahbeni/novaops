package com.novaops.userservice.infrastructure.adapter.specifications;

import com.novaops.userservice.domain.enums.RoleType;
import com.novaops.userservice.infrastructure.entity.UserEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

/**
 * Specifications for querying UserEntity. - root is the entity that we are working with - query is
 * the query that we are building - cb is the criteria builder that we use to build the query
 */
public class UserSpecifications {

  private UserSpecifications() {}

  /**
   * Creates a specification to find users by a search criteria. The criteria is matched against
   * email, first name, last name, and phone number.
   *
   * @param criteria the search criteria
   * @return a specification for finding users by criteria
   */
  public static Specification<UserEntity> hasCriteria(String criteria, RoleType role) {
    return (root, query, cb) -> {
      if (criteria == null || criteria.isBlank()) {
        return cb.conjunction();
      }
      Predicate result =
          cb.or(
              cb.like(cb.lower(root.get("email")), "%" + criteria.toLowerCase() + "%"),
              cb.like(cb.lower(root.get("fullName")), "%" + criteria.toLowerCase() + "%"));

      if (role != null) {
        Predicate roles = cb.equal(root.join("roles").get("name"), role.name());
        result = cb.and(result, roles);
      }

      return result;
    };
  }
}
