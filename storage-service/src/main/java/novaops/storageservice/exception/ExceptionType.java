package novaops.storageservice.exception;

public interface ExceptionType {
    String getTitleKey();

    String getMessageKey();

    String getMessageCause();
}
