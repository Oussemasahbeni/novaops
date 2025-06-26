package com.novaops.userservice.config.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.*;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

@Configuration
@EnableConfigurationProperties(OpenApiConfigurationProperties.class)
public class OpenApiConfig {

  @Value("${keycloak.url}")
  private String keycloakUrl;

  @Value("${keycloak.realm}")
  private String keycloakRealm;

  private final OpenApiConfigurationProperties properties;

  public OpenApiConfig(
      MappingJackson2HttpMessageConverter converter, OpenApiConfigurationProperties properties) {
    this.properties = properties;
    var supportedMediaTypes = new ArrayList<>(converter.getSupportedMediaTypes());
    supportedMediaTypes.add(new MediaType("application", "octet-stream"));
    converter.setSupportedMediaTypes(supportedMediaTypes);
  }

  @Bean
  public OpenAPI customOpenAPI() {

    final var gatewayServer = new io.swagger.v3.oas.models.servers.Server();
    gatewayServer.setUrl("http://localhost:8222");
    gatewayServer.setDescription("API Gateway");

    return new OpenAPI()
        .servers(List.of(gatewayServer))
        .info(
            new Info()
                .title(properties.getTitle())
                .version(properties.getApiVersion())
                .description(properties.getDescription())
                .termsOfService("Terms of service")
                .contact(new Contact().name("Oussema sahbeni").email("oussemas@inspark.tn"))
                .license(new License().name("Licence name").url("https://some-url.com")))
        .addSecurityItem(new SecurityRequirement().addList("keycloak"))
        .schemaRequirement(
            "keycloak",
            new SecurityScheme()
                .type(SecurityScheme.Type.OAUTH2)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .flows(
                    new OAuthFlows()
                        .authorizationCode(
                            new OAuthFlow()
                                .authorizationUrl(
                                    keycloakUrl
                                        + "/realms/"
                                        + keycloakRealm
                                        + "/protocol/openid-connect/auth")
                                .tokenUrl(
                                    keycloakUrl
                                        + "/realms/"
                                        + keycloakRealm
                                        + "/protocol/openid-connect/token")
                                .scopes(
                                    new Scopes()
                                        .addString("openid", "OpenID Connect scope")
                                        .addString("profile", "Profile access")))));
  }
}
