package novaops.storageservice.infrastructure.grpc; // Or a more suitable package

import org.springframework.lang.NonNull;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * An in-memory implementation of MultipartFile for bridging gRPC byte streams
 * with services that expect a MultipartFile.
 */
public class CustomMultipartFile implements MultipartFile {

    private final byte[] content;
    private final String name;
    private final String originalFilename;
    private final String contentType;

    public CustomMultipartFile(byte[] content, String name, String contentType) {
        this.content = content;
        this.name = name;
        this.originalFilename = name;
        this.contentType = contentType;
    }

    @NonNull
    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getOriginalFilename() {
        return originalFilename;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public boolean isEmpty() {
        return content == null || content.length == 0;
    }

    @Override
    public long getSize() {
        return content.length;
    }

    @NonNull
    @Override
    public byte[] getBytes() {
        return content;
    }

    @NonNull
    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(content);
    }

    @Override
    public void transferTo(@NonNull File dest) throws IOException, IllegalStateException {
        Files.write(dest.toPath(), this.content);
    }

    @Override
    public void transferTo(@NonNull Path dest) throws IOException, IllegalStateException {
        Files.write(dest, this.content);
    }
}