package com.novaops.userservice.exception;

import java.io.Serial;

public class GenericException extends ApplicationException {

    @Serial
    private static final long serialVersionUID = 5477619054099558741L;

    public GenericException(ExceptionType type) {
        super(type);
    }

    protected GenericException(ExceptionType type, Object[] valueParams, Object... keyParams) {
        super(type, valueParams, keyParams);
    }

    protected GenericException(
            ExceptionType type, Throwable cause, Object[] valueParams, Object... keyParams) {
        super(type, cause, valueParams, keyParams);
    }

    protected GenericException(ExceptionType type, Throwable cause) {
        super(type, cause);
    }

    protected GenericException(
            ExceptionType type, String message, Throwable cause, Object... keyParams) {
        super(type, message, cause, keyParams);
    }

    protected GenericException(ExceptionType type, Throwable cause, Object... valueParams) {
        super(type, cause, valueParams);
    }

    public GenericException(ExceptionType type, String message) {
        super(type, message);
    }

    public enum GenericExceptionType implements ExceptionType {
        GENERIC("error.server.generic.title", "error.server.generic.msg", "Forbidden error 403"),
        FAILED_TO_IDP_SEND_EMAIL(
                "error.server.generic.failed-to-idp-send-email.title",
                "error.server.generic.failed-to-idp-send-email.msg",
                "Failed to send email to IDP"),
        FAILED_TO_SAVE_USER(
                "error.server.generic.failed-to-save-user.title",
                "error.server.generic.failed-to-save-user.msg",
                "Failed to save user in the database"),
        NO_VALID_USERS_TO_IMPORT(
                "error.server.generic.no-valid-users-to-import.title",
                "error.server.generic.no-valid-users-to-import.msg",
                "No valid users to import from CSV file"),
        FAILED_TO_IMPORT_USERS(
                "error.server.generic.failed-to-import-users.title",
                "error.server.generic.failed-to-import-users.msg",
                "Failed to import users from CSV file"),
        FAILED_TO_UPLOAD_PROFILE_PICTURE(
                "error.server.generic.failed-to-upload-profile-picture.title",
                "error.server.generic.failed-to-upload-profile-picture.msg",
                "Failed to upload profile picture");

        private final String messageKey;
        private final String titleKey;
        private final String messageCause;

        GenericExceptionType(String titleKey, String messageKey, String messageCause) {
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
