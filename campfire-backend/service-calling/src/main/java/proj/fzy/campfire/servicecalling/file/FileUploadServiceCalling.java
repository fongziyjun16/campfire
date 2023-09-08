package proj.fzy.campfire.servicecalling.file;

import org.springframework.cloud.openfeign.FeignClient;
import proj.fzy.campfire.servicecalling.config.GlobalOpenFeignConfig;

@FeignClient(name = "file-service/upload", configuration = {GlobalOpenFeignConfig.class})
public interface FileUploadServiceCalling {
}
