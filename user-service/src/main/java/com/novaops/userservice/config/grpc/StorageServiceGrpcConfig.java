package com.novaops.userservice.config.grpc;

import org.novaops.storageservice.proto.StorageServiceGrpc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;

@Configuration
public class StorageServiceGrpcConfig {

    @Bean
    StorageServiceGrpc.StorageServiceStub stub(GrpcChannelFactory channels) {
        return StorageServiceGrpc.newStub(channels.createChannel("local"));
    }
}
