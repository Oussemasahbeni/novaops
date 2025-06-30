package com.novaops.userservice.infrastructure.adapter.persistence;

import com.google.protobuf.ByteString;
import com.novaops.userservice.domain.enums.Locale;
import com.novaops.userservice.domain.enums.RoleType;
import com.novaops.userservice.domain.model.DomainBlob;
import com.novaops.userservice.domain.model.User;
import com.novaops.userservice.domain.port.output.UserRepository;
import com.novaops.userservice.exception.ConflictException;
import com.novaops.userservice.exception.NotFoundException;
import com.novaops.userservice.exception.StorageException;
import com.novaops.userservice.infrastructure.adapter.specifications.UserSpecifications;
import com.novaops.userservice.infrastructure.dto.request.UpdateUserRequest;
import com.novaops.userservice.infrastructure.entity.UserEntity;
import com.novaops.userservice.infrastructure.mapper.UserMapper;
import com.novaops.userservice.infrastructure.repository.UserJpaRepository;
import com.novaops.userservice.shared.annotation.PersistenceAdapter;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.hibernate.StaleObjectStateException;
import org.novaops.storageservice.proto.Blob;
import org.novaops.storageservice.proto.FileInfo;
import org.novaops.storageservice.proto.StorageServiceGrpc;
import org.novaops.storageservice.proto.UploadRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@PersistenceAdapter
@RequiredArgsConstructor
public class UserJpaAdapter implements UserRepository {

    private final UserJpaRepository userRepository;
    private final UserMapper userMapper;
    private final StorageServiceGrpc.StorageServiceStub storageServiceStub;

    @Override
    @Transactional
    public User create(User user) {
        UserEntity userEntity = userMapper.toUserEntity(user);
        UserEntity savedUser = userRepository.save(userEntity);
        return userMapper.toUser(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(UUID id) {
        UserEntity userEntity =
                userRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new NotFoundException(NotFoundException.NotFoundExceptionType.USER_NOT_FOUND));

        User user = userMapper.toUser(userEntity);
        return Optional.of(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email).map(userMapper::toUser);
    }

    @Override
    public void deleteById(UUID id) {
        userRepository.deleteById(id);
    }

    @Override
    @Transactional
    public User update(UpdateUserRequest updateUserRequest) {
        try {
            UserEntity existingUser =
                    userRepository
                            .findById(updateUserRequest.id())
                            .orElseThrow(
                                    () ->
                                            new NotFoundException(
                                                    NotFoundException.NotFoundExceptionType.USER_NOT_FOUND));
            UserEntity user = userMapper.partialUpdate(updateUserRequest, existingUser);
            var savedUser = userRepository.save(user);
            return userMapper.toUser(savedUser);
        } catch (ObjectOptimisticLockingFailureException | StaleObjectStateException ex) {
            throw new ConflictException(ConflictException.ConflictExceptionType.CONFLICT_LOCK_VERSION);
        }
    }

    @Override
    public User update(User user) {
        try {
            UserEntity entity = userMapper.toUserEntity(user);
            var savedUser = userRepository.save(entity);
            return userMapper.toUser(savedUser);
        } catch (ObjectOptimisticLockingFailureException | StaleObjectStateException ex) {
            throw new ConflictException(ConflictException.ConflictExceptionType.CONFLICT_LOCK_VERSION);
        }
    }

    @Override
    public void updateLocale(String id, Locale locale) {
        UserEntity userEntity =
                userRepository
                        .findById(UUID.fromString(id))
                        .orElseThrow(
                                () ->
                                        new NotFoundException(NotFoundException.NotFoundExceptionType.USER_NOT_FOUND));
        userEntity.setLocale(locale);
        userRepository.save(userEntity);
    }

    @Override
    public Boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public Page<User> findAll(String search, Pageable pageable, RoleType role) {
        return userRepository
                .findAll(UserSpecifications.hasCriteria(search, role), pageable)
                .map(userMapper::toUser);
    }

    @Override
    public DomainBlob uploadProfilePicture(MultipartFile file) {
        // A CountDownLatch is used to make this asynchronous call behave synchronously.
        // We will wait for the server to send its response.
        final CountDownLatch finishLatch = new CountDownLatch(1);

        // AtomicReferences to hold the final result and any potential error from the server.
        final AtomicReference<DomainBlob> responseReference = new AtomicReference<>();
        final AtomicReference<Throwable> errorReference = new AtomicReference<>();

        // 1. Create a response observer to handle messages from the server.
        // Even though the server only sends one message, we still need an observer.
        StreamObserver<Blob> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(Blob grpcBlob) {
                // The server has sent its response. Convert the gRPC Blob to our domain Blob.
                DomainBlob domainBlob = new DomainBlob(
                        grpcBlob.getName(),
                        grpcBlob.getType(),
                        grpcBlob.getUrl(),
                        grpcBlob.getSize()
                );
                responseReference.set(domainBlob);
            }

            @Override
            public void onError(Throwable t) {
                // The server sent an error.
                errorReference.set(t);
                finishLatch.countDown(); // Release the latch to stop waiting.
            }

            @Override
            public void onCompleted() {
                // The server has finished its work.
                finishLatch.countDown(); // Release the latch.
            }
        };

        // 2. Initiate the RPC call. This returns a request observer that we use to send our data.
        StreamObserver<UploadRequest> requestObserver = storageServiceStub.uploadFile(responseObserver);

        try {
            // 3. Send the first message: File Metadata
            FileInfo fileInfo = FileInfo.newBuilder()
                    .setName(file.getOriginalFilename())
                    .setContentType(file.getContentType())
                    .build();

            UploadRequest metadataRequest = UploadRequest.newBuilder().setInfo(fileInfo).build();
            requestObserver.onNext(metadataRequest);

            // 4. Send the second (and subsequent) messages: File Chunks
            try (InputStream inputStream = file.getInputStream()) {
                byte[] buffer = new byte[4096]; // 4KB chunk size
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    UploadRequest chunkRequest = UploadRequest.newBuilder()
                            .setChunkData(ByteString.copyFrom(buffer, 0, bytesRead))
                            .build();
                    requestObserver.onNext(chunkRequest);
                }
            }

            // 5. Signal that we have finished sending data.
            requestObserver.onCompleted();

            // 6. Wait for the server to respond. Add a timeout to avoid waiting forever.
            if (!finishLatch.await(5, TimeUnit.MINUTES)) {
                throw new StorageException(StorageException.CloudStorageExceptionType.TIMEOUT);
            }

            // After the latch is released, check if an error occurred.
            if (errorReference.get() != null) {
                throw new StorageException(StorageException.CloudStorageExceptionType.GENERIC);
            }

            // 7. Return the captured response.
            return responseReference.get();

        } catch (IOException | InterruptedException e) {
            requestObserver.onError(e); // Let the server know the client had an error.
            Thread.currentThread().interrupt(); // Preserve the interrupted status
            throw new StorageException(
                    StorageException.CloudStorageExceptionType.GENERIC);
        }
    }
}
