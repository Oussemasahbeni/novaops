package com.novaops.notificationservice.config;

import com.novaops.userservice.config.audit.ApplicationAuditAware;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing(auditorAwareRef = "auditorAware")
@EnableCaching
@RequiredArgsConstructor
@Log4j2
@Configuration
public class BeansConfig {

  @Bean
  public AuditorAware<String> auditorAware() {
    return new ApplicationAuditAware();
  }
}
