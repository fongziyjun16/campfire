package proj.fzy.campfire.messagequeue.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import proj.fzy.campfire.model.mq.EmailAccountVerificationMessage;
import proj.fzy.campfire.model.mq.EmailPasswordResetMessage;

import java.util.function.Function;

@Configuration
public class EmailMessagePublisherConfig {

    @Bean
    public Function<EmailAccountVerificationMessage, EmailAccountVerificationMessage> publishEmailAccountVerificationMessage() {
        return emailVerificationMessage -> emailVerificationMessage;
    }

    @Bean
    public Function<EmailPasswordResetMessage, EmailPasswordResetMessage> publishEmailPasswordResetMessage() {
        return passwordResetMessage -> passwordResetMessage;
    }
}
