package com.novaops.notificationservice.domain.port.output;

/**
 * Output port interface for WebSocket notification operations. Defines the contract for sending
 * real-time notifications via WebSocket in the hexagonal architecture.
 */
public interface WsNotification {

  /**
   * Sends a real-time notification to a specific user via WebSocket.
   *
   * @param userId the unique identifier of the user to send the notification to
   * @param payload the notification payload to send
   */
  void send(String userId, Object payload);
}
