package novaops.storageservice.domain.model;


public record Blob(
        String name,
        String type,
        String url,
        Long size) {
}
