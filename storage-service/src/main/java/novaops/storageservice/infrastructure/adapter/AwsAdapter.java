package novaops.storageservice.infrastructure.adapter;


import io.awspring.cloud.autoconfigure.core.AwsAutoConfiguration;
import io.awspring.cloud.autoconfigure.s3.S3AutoConfiguration;
import io.awspring.cloud.s3.S3Template;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import novaops.storageservice.config.aws.AwsS3BucketProperties;
import novaops.storageservice.domain.model.Blob;
import novaops.storageservice.domain.service.Storage;
import novaops.storageservice.exception.StorageException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Log4j2
@EnableConfigurationProperties(AwsS3BucketProperties.class)
@Import({ // Re-import the configurations we excluded
        AwsAutoConfiguration.class,
        S3AutoConfiguration.class
})
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "aws")
public class AwsAdapter implements Storage {

    private final S3Template s3Template;
    private final AwsS3BucketProperties awsS3BucketProperties;

    private static final String IMAGES_FOLDER = "images";
    private static final String FILES_FOLDER = "files";

    @PostConstruct
    public void logActiveAdapter() {
        log.info("AWS Adapter is active");
    }

    /**
     * Determines the appropriate subfolder based on file content type
     *
     * @param contentType the MIME type of the file
     * @return the name of the subfolder where the file should be stored
     */
    private String determineSubfolderForContentType(String contentType) {

        if (contentType.startsWith("image/")) {
            return IMAGES_FOLDER;
        } else {
            return FILES_FOLDER;
        }
    }

    /**
     * Uploads a file to the cloud storage
     *
     * @param file the file to upload
     * @return the URL of the uploaded file
     */
    @Override
    public Blob uploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            log.warn("Upload attempt with null or empty file.");
            return null;
        }

        var originalFilename = file.getOriginalFilename();
        var bucketName = awsS3BucketProperties.getBucketName();

        // Determine subfolder based on file type
        String subfolder = determineSubfolderForContentType(Objects.requireNonNull(file.getContentType()));
        String keyName = subfolder + "/" + java.util.UUID.randomUUID() + "-" + originalFilename;

        try {
            s3Template.upload(bucketName, keyName, file.getInputStream());
            String fileUrl = awsS3BucketProperties.getCdnBaseUrl() + "/" + keyName;
            log.info("File '{}' uploaded to S3 in {} subfolder", originalFilename, subfolder);
            return new Blob(file.getOriginalFilename(), file.getContentType(), fileUrl, file.getSize());
        } catch (IOException e) {
            log.error("Error uploading file to S3", e);
            throw new StorageException(StorageException.CloudStorageExceptionType.AWS_FAILED_TO_UPLOAD_FILE);
        }
    }

    @Override
    public List<Blob> uploadFiles(MultipartFile[] files) {
        if (files == null || files.length == 0) {
            return List.of();
        }

        return Arrays.stream(files)
                .filter(file -> !file.isEmpty())
                .map(this::uploadFile)
                .toList();
    }


    /**
     * Deletes a file from the cloud storage
     *
     * @param url the URL of the file to delete
     */
    @Override
    public void deleteFile(String url) {
        if (url == null || url.isEmpty()) {
            log.warn("Delete attempt with null or empty URL.");
            return;
        }

        try {
            var bucketName = awsS3BucketProperties.getBucketName();

            // Extract the object key which includes the subfolder path
            String objectKey;
            if (url.contains(IMAGES_FOLDER + "/")) {
                objectKey = url.substring(url.indexOf(IMAGES_FOLDER + "/"));
            } else if (url.contains(FILES_FOLDER + "/")) {
                objectKey = url.substring(url.indexOf(FILES_FOLDER + "/"));
            } else {
                log.warn("Could not determine object key from URL for deletion: {}", url);
                return;
            }

            log.info("Deleting S3 object: {}", objectKey);
            s3Template.deleteObject(bucketName, objectKey);
        } catch (Exception e) {
            log.error("Error during deletion process for URL {}: {}", url, e.getMessage());
        }
    }
}
