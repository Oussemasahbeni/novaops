package com.novaops.userservice.domain.model;


public record DomainBlob(
        String name,
        String type,
        String url,
        Long size) {
}
