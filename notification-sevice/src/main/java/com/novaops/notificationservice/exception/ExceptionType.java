package com.novaops.notificationservice.exception;

public interface ExceptionType {
  String getTitleKey();

  String getMessageKey();

  String getMessageCause();
}
