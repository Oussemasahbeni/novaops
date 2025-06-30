package novaops.storageservice;

import io.awspring.cloud.autoconfigure.core.AwsAutoConfiguration;
import io.awspring.cloud.autoconfigure.s3.S3AutoConfiguration;
import novaops.storageservice.config.StorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(
        exclude = {
                S3AutoConfiguration.class,
                AwsAutoConfiguration.class
        }
)
@EnableConfigurationProperties({
        StorageProperties.class,
})
public class StorageServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(StorageServiceApplication.class, args);
    }

}
