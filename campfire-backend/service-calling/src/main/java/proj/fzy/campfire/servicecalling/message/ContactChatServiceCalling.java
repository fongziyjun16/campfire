package proj.fzy.campfire.servicecalling.message;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import proj.fzy.campfire.model.dto.CommonResponse;
import proj.fzy.campfire.servicecalling.config.GlobalOpenFeignConfig;

@FeignClient(name = "message-service/contact-chat", configuration = {GlobalOpenFeignConfig.class})
public interface ContactChatServiceCalling {
    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    CommonResponse<Void> startNewContactChat(@RequestParam Long targetId, @RequestParam String targetUsername);

    @DeleteMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    CommonResponse<Void> deleteContact(@RequestParam Long targetId);
}
