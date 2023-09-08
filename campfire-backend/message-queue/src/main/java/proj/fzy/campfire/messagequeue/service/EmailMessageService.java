package proj.fzy.campfire.messagequeue.service;

import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;
import proj.fzy.campfire.model.mq.EmailAccountVerificationMessage;
import proj.fzy.campfire.model.mq.EmailPasswordResetMessage;

@Service
public class EmailMessageService {

    private final StreamBridge streamBridge;

    public EmailMessageService(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    public void publishAccountVerification(String to, String code) {
        streamBridge.send(
                "publishEmailAccountVerificationMessage-out-0",
                EmailAccountVerificationMessage.builder()
                        .to(to)
                        .code(code)
                        .build());
    }

    public void publishPasswordReset(String to, String code) {
        streamBridge.send(
                "publishEmailPasswordResetMessage-out-0",
                EmailPasswordResetMessage.builder()
                        .to(to)
                        .code(code)
                        .build());
    }

}
