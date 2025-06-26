package com.novaops.notificationservice.exception;

import java.io.Serial;

public class NotFoundException extends ApplicationException {

  @Serial private static final long serialVersionUID = 5477619054099558741L;

  public NotFoundException(NotFoundExceptionType type) {
    super(type);
  }

  public NotFoundException(NotFoundExceptionType type, Throwable cause) {
    super(type, cause);
  }

  public NotFoundException(NotFoundExceptionType type, String message, Throwable cause) {
    super(type, message, cause);
  }

  public NotFoundException(
      NotFoundExceptionType type, String message, Throwable cause, Object... keyParams) {
    super(type, message, cause, keyParams);
  }

  public NotFoundException(NotFoundExceptionType type, Object[] valueParams, Object... keyParams) {
    super(type, valueParams, keyParams);
  }

  public NotFoundException(NotFoundExceptionType type, Object... valueParams) {
    super(type, valueParams);
  }

  public enum NotFoundExceptionType implements ExceptionType {
    GENERIC(
        "error.server.not-found.generic.title",
        "error.server.not-found.generic.msg",
        "Resource not found 400"),

    NOTIFICATION_NOT_FOUND(
        "error.server.not-found.notification.title",
        "error.server.not-found.notification.msg",
        "Notification not found : {0}");

    private final String messageKey;
    private final String titleKey;
    private final String messageCause;

    NotFoundExceptionType(String titleKey, String messageKey, String messageCause) {
      this.messageKey = messageKey;
      this.titleKey = titleKey;
      this.messageCause = messageCause;
    }

    @Override
    public String getTitleKey() {
      return titleKey;
    }

    @Override
    public String getMessageKey() {
      return messageKey;
    }

    @Override
    public String getMessageCause() {
      return messageCause;
    }
  }
}
