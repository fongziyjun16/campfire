package proj.fzy.campfire.service.file.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "file-storage")
@Data
public class FileStorageProperties {
    private String bucketLocation;
    private String domain;
}
