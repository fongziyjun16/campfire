package proj.fzy.campfire.service.email.utils;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import proj.fzy.campfire.model.mq.EmailAccountVerificationMessage;
import proj.fzy.campfire.model.mq.EmailPasswordResetMessage;

import java.nio.charset.StandardCharsets;

@Component
public class EmailUtils {

    @Value("${spring.mail.username}")
    private String from;
    private final TemplateEngine templateEngine;
    private final JavaMailSender mailSender;

    public EmailUtils(TemplateEngine templateEngine, JavaMailSender mailSender) {
        this.templateEngine = templateEngine;
        this.mailSender = mailSender;
    }

    public void sendAccountVerification(EmailAccountVerificationMessage emailAccountVerificationMessage) {
        try {
            Context context = new Context();
            context.setVariable("code", emailAccountVerificationMessage.getCode());

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, StandardCharsets.UTF_8.name());
            mimeMessageHelper.setFrom(from);
            mimeMessageHelper.setTo(emailAccountVerificationMessage.getTo());
            mimeMessageHelper.setSubject("Account Verification Code Notification");
            mimeMessageHelper.setText(templateEngine.process("email_account_verification", context), true);

            mailSender.send(mimeMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendPasswordReset(EmailPasswordResetMessage emailPasswordResetMessage) {
        try {
            Context context = new Context();
            context.setVariable("code", emailPasswordResetMessage.getCode());

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, StandardCharsets.UTF_8.name());
            mimeMessageHelper.setFrom(from);
            mimeMessageHelper.setTo(emailPasswordResetMessage.getTo());
            mimeMessageHelper.setSubject("Account Password Reset");
            mimeMessageHelper.setText(templateEngine.process("email_password_reset", context), true);

            mailSender.send(mimeMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
