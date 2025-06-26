package com.novaops.notificationservice.shared.mapstruct;

import java.util.Collection;
import org.hibernate.Hibernate;

public interface LazyLoadingAwareMapper {
  default boolean isNotLazyLoaded(Collection<?> sourceCollection) {
    // Case: Source field in domain object is lazy: Skip mapping
    // Continue Mapping
    return Hibernate.isInitialized(sourceCollection);

    // Skip mapping
  }
}
