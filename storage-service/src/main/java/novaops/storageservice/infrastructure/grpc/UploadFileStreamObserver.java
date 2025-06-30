package novaops.storageservice.infrastructure.grpc;


import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;
import novaops.storageservice.domain.service.Storage;
import org.novaops.storageservice.proto.Blob;
import org.novaops.storageservice.proto.FileInfo;
import org.novaops.storageservice.proto.UploadRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class UploadFileStreamObserver implements StreamObserver<UploadRequest> {

    private final Storage storage;
    private final StreamObserver<Blob> responseObserver;
    private FileInfo fileInfo;
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    public UploadFileStreamObserver(Storage storage, StreamObserver<Blob> responseObserver) {
        this.storage = storage;
        this.responseObserver = responseObserver;
    }

    @Override
    public void onNext(UploadRequest request) {
        // The client sends a stream of messages. We use the 'oneof' to see what's inside.
        switch (request.getRequestCase()) {
            case INFO:
                // This is the first message, containing metadata. Store it.
                this.fileInfo = request.getInfo();
                break;
            case CHUNK_DATA:
                // These are subsequent messages with the file's raw bytes.
                // Write the received bytes into our in-memory buffer.
                try {
                    ByteString chunk = request.getChunkData();
                    buffer.write(chunk.toByteArray());
                } catch (IOException e) {
                    // If writing to the buffer fails, abort the operation.
                    responseObserver.onError(e);
                    this.closeBuffer();
                }
                break;
        }
    }

    @Override
    public void onError(Throwable t) {
        // The client's stream had an error.
        // Log the error and clean up resources.
        System.err.println("Upload failed with error: " + t.getMessage());
        this.closeBuffer();
    }

    @Override
    public void onCompleted() {
        // The client has finished sending all chunks.
        // Now we can process the complete file.
        try {
            // 1. Create a MultipartFile from the received data.
            byte[] fileBytes = buffer.toByteArray();
            MultipartFile multipartFile = new CustomMultipartFile(
                    fileBytes,
                    fileInfo.getName(),
                    fileInfo.getContentType()
            );

            // 2. Call your existing business logic.
            novaops.storageservice.domain.model.Blob domainBlob = storage.uploadFile(multipartFile);

            // 3. Convert your domain model 'Blob' to the gRPC 'Blob' protobuf message.
            Blob grpcBlob = Blob.newBuilder()
                    .setName(domainBlob.name())
                    .setUrl(domainBlob.url())
                    .setType(domainBlob.type())
                    .setSize(domainBlob.size())
                    .build();

            // 4. Send the final response to the client.
            responseObserver.onNext(grpcBlob);

            // 5. Signal that the RPC is successfully completed.
            responseObserver.onCompleted();

        } catch (Exception e) {
            responseObserver.onError(e); // Inform the client of the error.
        } finally {
            this.closeBuffer(); // Clean up the buffer.
        }
    }

    private void closeBuffer() {
        try {
            buffer.close();
        } catch (IOException e) {
            // Log this, but the primary error has likely already been sent.
            e.printStackTrace();
        }
    }
}

