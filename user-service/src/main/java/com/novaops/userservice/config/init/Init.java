package com.novaops.userservice.config.init;

import com.novaops.userservice.infrastructure.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class Init {

    private final CreateDefaultUser createDefaultUser;

    @Bean
    @Transactional
    public CommandLineRunner run(
            Keycloak keycloak,
            UserJpaRepository userRepository) {
        return args -> {
            createDefaultUser.createDefaultAdmin(keycloak, userRepository);
        };
    }
}
