package novaops.storageservice.infrastructure.adapter;


import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import novaops.storageservice.domain.model.Blob;
import novaops.storageservice.domain.service.Storage;
import novaops.storageservice.exception.StorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static java.lang.System.currentTimeMillis;

/**
 * Adapter class for local file storage
 */
@Component
@Log4j2
@Primary
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "local")
public class LocalServerAdapter implements Storage {

    @Value("${app.file.uploads.path:uploads}")
    private String uploadDirPath;

    @Value("${app.file.uploads.public-url-prefix:/uploads}")
    private String publicUrlPrefix;

    @Value("${app.backend-url}")
    public String backendUrl;

    private Path uploadPath;

    private static final String IMAGES_FOLDER = "images";
    private static final String FILES_FOLDER = "files";

    @PostConstruct
    public void init() {
        try {
            this.uploadPath = Paths.get(uploadDirPath).toAbsolutePath().normalize();

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Create subfolders for different file types
            createSubfolder(IMAGES_FOLDER);
            createSubfolder(FILES_FOLDER);

            if (!this.publicUrlPrefix.startsWith("/")) {
                this.publicUrlPrefix = "/" + this.publicUrlPrefix;
            }
            if (this.publicUrlPrefix.length() > 1 && this.publicUrlPrefix.endsWith("/")) {
                this.publicUrlPrefix = this.publicUrlPrefix.substring(0, this.publicUrlPrefix.length() - 1);
            }

            log.info("Local Server Adapter is active.");
            log.info("Storage path: {}", uploadPath);
            log.info("Public URL prefix: {}", publicUrlPrefix);

        } catch (IOException e) {
            log.error("Could not initialize storage location: {}", uploadDirPath, e);
            throw new StorageException(StorageException.CloudStorageExceptionType.GENERIC);
        }
    }

    /**
     * Creates a subfolder within the upload directory if it doesn't exist
     *
     * @param folderName name of the subfolder to create
     * @throws IOException if folder creation fails
     */
    private void createSubfolder(String folderName) throws IOException {
        Path subfolder = this.uploadPath.resolve(folderName);
        if (!Files.exists(subfolder)) {
            Files.createDirectories(subfolder);
            log.info("Created subfolder: {}", subfolder);
        }
    }

    /**
     * Determines the appropriate subfolder based on file content type
     *
     * @param contentType the MIME type of the file
     * @return the name of the subfolder where the file should be stored
     */
    private String determineSubfolderForContentType(String contentType) {
        if (contentType == null) {
            return FILES_FOLDER;
        }

        if (contentType.startsWith("image/")) {
            return IMAGES_FOLDER;
        } else {
            return FILES_FOLDER;
        }

    }

    @Override
    public Blob uploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            log.warn("Upload attempt with null or empty file.");
            return null;
        }

        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

        try {
            // Create a unique filename
            String generatedFileName = UUID.randomUUID() + "_" + currentTimeMillis() + file.getOriginalFilename();

            // Determine subfolder based on file type
            String subfolder = determineSubfolderForContentType(file.getContentType());

            if (originalFilename.isEmpty()) {
                log.warn("Uploaded file had no original filename. Saved as '{}'", generatedFileName);
            }

            // Create the target path with subfolder
            Path subfolderPath = this.uploadPath.resolve(subfolder);
            Path targetPath = subfolderPath.resolve(generatedFileName).normalize();

            // Security check: Ensure the file is stored within the appropriate subfolder
            if (!targetPath.getParent().equals(subfolderPath)) {
                log.error("Cannot store file outside designated subfolder. Attempted path: {}", targetPath);
                throw new StorageException(StorageException.CloudStorageExceptionType.GENERIC);
            }

            // Ensure the subfolder exists (should have been created in init, but double-check)
            if (!Files.exists(subfolderPath)) {
                Files.createDirectories(subfolderPath);
            }

            // Use transferTo for efficiency
            file.transferTo(targetPath.toFile());

            // Construct the public URL with a subfolder path
            String url = this.backendUrl + this.publicUrlPrefix + "/" + subfolder + "/" + generatedFileName;
            log.info("File '{}' saved in {} subfolder as '{}'", originalFilename, subfolder, generatedFileName);
            return new Blob(file.getOriginalFilename(), file.getContentType(), url, file.getSize());

        } catch (Exception e) {
            log.error("Failed to upload file '{}'", originalFilename, e);
            throw new StorageException(StorageException.CloudStorageExceptionType.GENERIC);
        }
    }

    @Override
    public List<Blob> uploadFiles(MultipartFile[] files) {
        if (files == null || files.length == 0) {
            return new ArrayList<>();
        }
        List<Blob> blobs = new ArrayList<>();
        for (MultipartFile file : files) {
            try {
                Blob blob = uploadFile(file);
                if (blob != null) {
                    blobs.add(blob);
                }
            } catch (StorageException e) {
                // Log and continue with other files, or decide on different error handling
                log.error("Failed to upload one of the files in batch: {}", e.getMessage());
            }
        }
        return blobs;
    }

    @Override
    public void deleteFile(String url) {
        if (url == null || url.isEmpty()) {
            log.warn("Delete attempt with null or empty URL.");
            return;
        }

        try {
            // Extract filename and subfolder from URL
            String relativePath = null;
            if (url.startsWith("http://") || url.startsWith("https://")) {
                // If it's a full URL, find the path after the publicUrlPrefix
                int prefixIndex = url.indexOf(this.publicUrlPrefix);
                if (prefixIndex != -1) {
                    relativePath = url.substring(prefixIndex + this.publicUrlPrefix.length());
                    relativePath = relativePath.startsWith("/") ? relativePath.substring(1) : relativePath;
                }
            } else if (url.startsWith(this.publicUrlPrefix)) {
                // Handle case when URL starts with just the prefix but not the full URL
                relativePath = url.substring(this.publicUrlPrefix.length());
                relativePath = relativePath.startsWith("/") ? relativePath.substring(1) : relativePath;
            } else {
                // Assume it's just the path after the public URL prefix
                relativePath = url.startsWith("/") ? url.substring(1) : url;
            }

            if (relativePath == null || relativePath.isEmpty()) {
                log.warn("Could not extract relative path from URL for deletion: {}", url);
                return;
            }

            Path filePath = this.uploadPath.resolve(relativePath).normalize();

            // Security check - ensure the file is within the upload directory
            if (!filePath.startsWith(this.uploadPath)) {
                log.error("Attempt to delete file outside designated upload directory: {}", filePath);
                return;
            }

            boolean deleted = Files.deleteIfExists(filePath);
            if (deleted) {
                log.info("File deleted successfully: {}", filePath);
            } else {
                log.warn("File not found for deletion, or already deleted: {}", filePath);
            }
        } catch (IOException e) {
            log.error("Failed to delete file with URL {}: {}", url, e);
        } catch (Exception e) {
            log.error("Error during deletion process for URL {}: {}", url, e.getMessage());
        }
    }


}