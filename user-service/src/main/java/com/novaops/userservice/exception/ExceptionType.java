package com.novaops.userservice.exception;

public interface ExceptionType {
    String getTitleKey();

    String getMessageKey();

    String getMessageCause();
}
