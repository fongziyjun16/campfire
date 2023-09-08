package proj.fzy.campfire.service.common.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import cn.hutool.jwt.JWTValidator;
import cn.hutool.jwt.signers.JWTSignerUtil;
import org.springframework.stereotype.Component;
import proj.fzy.campfire.service.common.config.properties.JwtProperties;

@Component
public class JwtUtils {
    private final JwtProperties jwtProperties;

    public JwtUtils(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public String generate(Long id, String username) {
        return JWT.create()
                .setIssuedAt(DateUtil.date())
                .setPayload("id", id)
                .setPayload("username", username)
                .setExpiresAt(DateUtil.offsetSecond(DateUtil.date(), jwtProperties.getDuration()))
                .setSigner(JWTSignerUtil.hs256(jwtProperties.getSecretKey().getBytes()))
                .sign();
    }

    public boolean verify(String token) {
        boolean result = true;
        if (token != null && !token.isEmpty()) {
            try {
                JWTValidator
                        .of(token)
                        .validateAlgorithm(JWTSignerUtil.hs256(jwtProperties.getSecretKey().getBytes()))
                        .validateDate(DateUtil.date());
            } catch (Exception e) {
                e.printStackTrace();
                result = false;
            }
        }
        return result;
    }

    public Long getId(String token) {
        return Long.parseLong(JWTUtil
                .parseToken(token)
                .setKey(jwtProperties.getSecretKey().getBytes())
                .getPayload("id")
                .toString());
    }

    public String getUsername(String token) {
        return JWTUtil
                .parseToken(token)
                .setKey(jwtProperties.getSecretKey().getBytes())
                .getPayload("username").toString();
    }

    public Integer getDuration() {
        return jwtProperties.getDuration();
    }
}
