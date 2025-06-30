package novaops.storageservice.infrastructure.adapter;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.specialized.BlockBlobClient;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import novaops.storageservice.domain.model.Blob;
import novaops.storageservice.domain.service.Storage;
import novaops.storageservice.exception.StorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Adapter class for Azure cloud storage, with logic aligned to the AWS adapter.
 */
@Component
@Log4j2
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "azure")
public class AzureAdapter implements Storage {

    @Value("${spring.cloud.azure.storage.blob.connection-string}")
    private String connectionString;

    private BlobServiceClient blobServiceClient;

    private static final String IMAGES_CONTAINER = "images";
    private static final String FILES_CONTAINER = "files";
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB
    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB

    @PostConstruct
    public void init() {
        this.blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();
        log.info("Azure Adapter is active and BlobServiceClient is initialized.");
    }

    /**
     * Determines the appropriate container based on file content type.
     *
     * @param contentType the MIME type of the file.
     * @return the name of the container where the file should be stored.
     */
    private String determineContainerForContentType(String contentType) {
        if (contentType != null && contentType.startsWith("image/")) {
            return IMAGES_CONTAINER;
        }
        return FILES_CONTAINER;
    }

    /**
     * Method to get the blob client for a blob.
     *
     * @param blobName      the name of the blob.
     * @param containerName the name of the container.
     * @return the blob client.
     */
    private BlockBlobClient getBlobClient(String blobName, String containerName) {
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
        return containerClient.getBlobClient(blobName).getBlockBlobClient();
    }

    /**
     * Method to get the blob name from a blob URL.
     *
     * @param blobUrl the URL of the blob.
     * @return the blob name.
     */
    private String getBlobNameFromUrl(String blobUrl) {
        // Assuming the blob name is the last segment of the URL
        return blobUrl.substring(blobUrl.lastIndexOf("/") + 1);
    }

    /**
     * Uploads a file to the appropriate Azure container based on its content type.
     *
     * @param file the file to upload.
     * @return a Blob object with details of the uploaded file.
     */
    @Override
    public Blob uploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            log.warn("Upload attempt with null or empty file.");
            return null;
        }

        String contentType = file.getContentType();
        String containerName = determineContainerForContentType(contentType);

        // Validate file size based on type
        if (IMAGES_CONTAINER.equals(containerName) && file.getSize() > MAX_IMAGE_SIZE) {
            throw new StorageException(StorageException.CloudStorageExceptionType.IMAGE_SIZE_LIMIT_EXCEEDED);
        } else if (FILES_CONTAINER.equals(containerName) && file.getSize() > MAX_FILE_SIZE) {
            throw new StorageException(StorageException.CloudStorageExceptionType.FILE_SIZE_LIMIT_EXCEEDED);
        }

        String blobName = java.util.UUID.randomUUID() + "-" + file.getOriginalFilename();
        BlockBlobClient blobClient = getBlobClient(blobName, containerName);

        try {
            byte[] bytes = file.getBytes();
            try (InputStream inputStream = new ByteArrayInputStream(bytes)) {
                // Upload the file, overwriting if it already exists
                blobClient.upload(inputStream, bytes.length, true);
                BlobHttpHeaders headers = new BlobHttpHeaders();
                headers.setContentType(contentType);
                blobClient.setHttpHeaders(headers);
            }
            log.info("File '{}' uploaded to Azure in {} container", file.getOriginalFilename(), containerName);
            return new Blob(file.getOriginalFilename(), contentType, blobClient.getBlobUrl(), file.getSize());
        } catch (Exception e) {
            log.error("Failed to upload file to Azure", e);
            throw new StorageException(StorageException.CloudStorageExceptionType.AZURE_FAILED_TO_UPLOAD_FILE);
        }
    }

    /**
     * Uploads multiple files to cloud storage.
     *
     * @param files array of files to upload.
     * @return a list of Blob objects for the uploaded files.
     */
    @Override
    public List<Blob> uploadFiles(MultipartFile[] files) {
        if (files == null || files.length == 0) {
            return List.of();
        }

        return Arrays.stream(files)
                .filter(file -> file != null && !file.isEmpty())
                .map(this::uploadFile)
                .filter(Objects::nonNull) // Filter out nulls from failed/empty uploads
                .collect(Collectors.toList());
    }

    /**
     * Deletes a file from Azure storage by its URL.
     * It determines the correct container ('images' or 'files') from the URL structure.
     *
     * @param url the full URL of the file to delete.
     */
    @Override
    public void deleteFile(String url) {
        if (url == null || url.isEmpty()) {
            log.warn("Delete attempt with null or empty URL.");
            return;
        }

        String containerName;
        // The URL structure is like: https://<account>.blob.core.windows.net/<container>/<blobname>
        if (url.contains("/" + IMAGES_CONTAINER + "/")) {
            containerName = IMAGES_CONTAINER;
        } else if (url.contains("/" + FILES_CONTAINER + "/")) {
            containerName = FILES_CONTAINER;
        } else {
            log.warn("Could not determine container from URL for deletion: {}", url);
            return;
        }

        try {
            String blobName = getBlobNameFromUrl(url);
            BlockBlobClient blobClient = getBlobClient(blobName, containerName);
            blobClient.deleteIfExists();
            log.info("Deleted Azure blob: {} from container: {}", blobName, containerName);
        } catch (Exception e) {
            log.error("Error during Azure blob deletion process for URL {}: {}", url, e.getMessage(), e);
            // Depending on requirements, you might want to re-throw a custom exception here
        }
    }
}