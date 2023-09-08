package proj.fzy.campfire.service.common.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "machine-id")
@Getter
@Setter
public class MachineIdProperties {
    private Long datacenterId;
    private Long workerId;
}
