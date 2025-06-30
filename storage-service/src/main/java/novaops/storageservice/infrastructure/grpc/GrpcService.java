package novaops.storageservice.infrastructure.grpc;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import novaops.storageservice.domain.service.Storage;
import org.novaops.storageservice.proto.Blob;
import org.novaops.storageservice.proto.DeleteRequest;
import org.novaops.storageservice.proto.StorageServiceGrpc;
import org.novaops.storageservice.proto.UploadRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GrpcService extends StorageServiceGrpc.StorageServiceImplBase {


    private final Storage storage;


    /**
     * This method is called by the gRPC framework when a client initiates the UploadFile RPC.
     * It must return a new StreamObserver that will handle the incoming stream of UploadRequest messages.
     *
     * @param responseObserver An observer to send the final Blob response (or an error) back to the client.
     * @return An observer to handle the client's messages.
     */
    @Override
    public StreamObserver<UploadRequest> uploadFile(StreamObserver<Blob> responseObserver) {
        return new UploadFileStreamObserver(storage, responseObserver);
    }

    /**
     * Implements the simple Unary RPC for deleting a file.
     */
    @Override
    public void deleteFile(DeleteRequest request, StreamObserver<Empty> responseObserver) {
        try {
            storage.deleteFile(request.getUrl());
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            //  map to specific gRPC status codes
            responseObserver.onError(
                    io.grpc.Status.INTERNAL
                            .withDescription("Failed to delete file: " + e.getMessage())
                            .asRuntimeException()
            );
        }
    }

}
