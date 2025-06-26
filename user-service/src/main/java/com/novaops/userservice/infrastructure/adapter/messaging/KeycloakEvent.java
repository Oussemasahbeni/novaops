package com.novaops.userservice.infrastructure.adapter.messaging;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the main Keycloak event structure.
 *
 * @param id The unique ID of the event.
 * @param time The epoch time in milliseconds when the event occurred.
 * @param type The type of the event (e.g., "REGISTER").
 * @param realmId The ID of the realm where the event occurred.
 * @param realmName The name of the realm.
 * @param clientId The ID of the client involved.
 * @param userId The ID of the user involved.
 * @param sessionId The session ID, can be null.
 * @param ipAddress The IP address of the user.
 * @param error The error string, can be null.
 * @param details A nested object containing event-specific details.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record KeycloakEvent(
    String id,
    long time,
    String type,
    String realmId,
    String realmName,
    String clientId,
    String userId,
    String sessionId,
    String ipAddress,
    String error,
    EventDetailsRecord details) {
  /**
   * Represents the nested "details" object within a Keycloak event. The @JsonProperty annotation
   * maps snake_case JSON fields to camelCase Java record components.
   */
  @JsonIgnoreProperties(ignoreUnknown = true)
  public record EventDetailsRecord(
      @JsonProperty("auth_method") String authMethod,
      @JsonProperty("auth_type") String authType,
      @JsonProperty("register_method") String registerMethod,
      @JsonProperty("last_name") String lastName,
      @JsonProperty("redirect_uri") String redirectUri,
      @JsonProperty("first_name") String firstName,
      @JsonProperty("code_id") String codeId,
      String email,
      String username) {}
}
