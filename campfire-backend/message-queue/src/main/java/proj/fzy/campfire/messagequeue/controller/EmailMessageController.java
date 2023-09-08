package proj.fzy.campfire.messagequeue.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import proj.fzy.campfire.messagequeue.service.EmailMessageService;
import proj.fzy.campfire.model.mq.EmailAccountVerificationMessage;
import proj.fzy.campfire.model.mq.EmailPasswordResetMessage;

@RestController
@RequestMapping("/email-message")
public class EmailMessageController {

    private final EmailMessageService emailMessageService;

    public EmailMessageController(EmailMessageService emailMessageService) {
        this.emailMessageService = emailMessageService;
    }

    @PostMapping(value = "/account-verification", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void sendAccountVerificationCode(@RequestBody EmailAccountVerificationMessage emailAccountVerificationMessage) {
        emailMessageService.publishAccountVerification(
                emailAccountVerificationMessage.getTo(),
                emailAccountVerificationMessage.getCode());
    }

    @PostMapping(value = "/password-reset", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void sendPasswordReset(@RequestBody EmailPasswordResetMessage emailPasswordResetMessage) {
        emailMessageService.publishPasswordReset(
                emailPasswordResetMessage.getTo(),
                emailPasswordResetMessage.getCode());
    }
}
