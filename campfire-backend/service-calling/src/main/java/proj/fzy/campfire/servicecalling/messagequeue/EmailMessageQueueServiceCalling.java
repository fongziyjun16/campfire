package proj.fzy.campfire.servicecalling.messagequeue;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import proj.fzy.campfire.model.mq.EmailAccountVerificationMessage;
import proj.fzy.campfire.model.mq.EmailPasswordResetMessage;
import proj.fzy.campfire.servicecalling.config.GlobalOpenFeignConfig;

@FeignClient(name = "message-queue/email-message", configuration = {GlobalOpenFeignConfig.class})
public interface EmailMessageQueueServiceCalling {
    @PostMapping(value = "/account-verification", consumes = MediaType.APPLICATION_JSON_VALUE)
    void sendAccountVerificationCode(@RequestBody EmailAccountVerificationMessage emailAccountVerificationMessage);
    @PostMapping(value = "/password-reset", consumes = MediaType.APPLICATION_JSON_VALUE)
    void sendPasswordReset(@RequestBody EmailPasswordResetMessage emailPasswordResetMessage);
}
