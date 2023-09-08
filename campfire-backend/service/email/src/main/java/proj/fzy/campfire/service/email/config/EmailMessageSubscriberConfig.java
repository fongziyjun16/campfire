package proj.fzy.campfire.service.email.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import proj.fzy.campfire.model.mq.EmailAccountVerificationMessage;
import proj.fzy.campfire.model.mq.EmailPasswordResetMessage;
import proj.fzy.campfire.service.email.utils.EmailUtils;

import java.util.function.Consumer;

@Configuration
public class EmailMessageSubscriberConfig {

    private final EmailUtils emailUtils;

    public EmailMessageSubscriberConfig(EmailUtils emailUtils) {
        this.emailUtils = emailUtils;
    }

    @Bean
    public Consumer<EmailAccountVerificationMessage> receiveEmailAccountVerificationMessage() {
        return emailAccountVerificationMessage -> emailUtils.sendAccountVerification(emailAccountVerificationMessage);
    }

    @Bean
    public Consumer<EmailPasswordResetMessage> receiveEmailPasswordResetMessage() {
        return emailPasswordResetMessage -> emailUtils.sendPasswordReset(emailPasswordResetMessage);
    }

}
