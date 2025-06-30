package novaops.storageservice.domain.enums;

/**
 * Defines the supported storage providers for the application.
 */
public enum StorageProvider {
    /**
     * Use local file system for storage. Ideal for development.
     */
    local,

    /**
     * Use Amazon Web Services (AWS) S3 for storage.
     */
    aws,

    /**
     * Use Microsoft Azure Blob Storage.
     */
    azure
}