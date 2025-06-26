package com.novaops.notificationservice.domain.port.output;

import com.novaops.notificationservice.domain.enums.EmailType;
import jakarta.mail.MessagingException;
import java.util.Map;
import org.springframework.scheduling.annotation.Async;

/**
 * Output port interface for Email notification operations. Defines the contract for sending email
 * notifications in the hexagonal architecture.
 */
public interface Emails {

  /**
   * Sends an email asynchronously to the specified recipient.
   *
   * @param to the recipient email address
   * @param emailType the type of email to send
   * @param properties the properties/variables to use in the email template
   * @throws MessagingException if there's an error sending the email
   */
  @Async
  void sendEmail(String to, EmailType emailType, Map<String, Object> properties)
      throws MessagingException;
}
