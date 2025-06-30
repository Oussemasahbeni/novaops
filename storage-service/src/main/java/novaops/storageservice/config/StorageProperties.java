package novaops.storageservice.config;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import novaops.storageservice.domain.enums.StorageProvider;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.storage")
@Validated
@Data
public class StorageProperties {

    /**
     * The active storage provider. Determines which storage adapter will be used.
     * Defaults to 'local' if not specified.
     */
    @NotNull
    private StorageProvider provider = StorageProvider.local;
}