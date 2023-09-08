package proj.fzy.campfire.service.common.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedisUtils {

    private static String WAITING_VERIFICATION_ACCOUNT_PREFIX = "waiting_for_verification_account:";
    private static String FORGET_PASSWORD_ACCOUNT_PREFIX = "forget_password_account:";
    private static String SIGNED_IN_ACCOUNT_PREFIX = "signed_in_account:";
    private static String WS_CSRF_TOKEN_PREFIX = "ws_csrf_token:";
    private static String WS_CONNECTED_USER_PREFIX = "ws_connected_user:";

    public static String getSignedInAccountKey(Long id) {
        return SIGNED_IN_ACCOUNT_PREFIX + id;
    }

    public static String getForgetPasswordAccountKey(Long id) {
        return FORGET_PASSWORD_ACCOUNT_PREFIX + id;
    }

    public static String getWaitingVerificationAccountKey(Long id) {
        return WAITING_VERIFICATION_ACCOUNT_PREFIX + id;
    }

    public static String getWsCsrfTokenKey(Long id) { return WS_CSRF_TOKEN_PREFIX + id;}

    private final RedisTemplate<String, String> template;

    public RedisUtils(RedisTemplate<String, String> template) {
        this.template = template;
    }

    public void set(String key, String value) {
        template.opsForValue().set(key, value);
    }

    public void setWithDuration(String key, String value, long seconds) {
        template.opsForValue().set(key, value, seconds, TimeUnit.SECONDS);
    }

    public String get(String key) {
        return template.opsForValue().get(key);
    }

    public void del(String key) {
        template.delete(key);
    }

    public boolean exists(String key) {
        return Boolean.TRUE.equals(template.hasKey(key));
    }
}
