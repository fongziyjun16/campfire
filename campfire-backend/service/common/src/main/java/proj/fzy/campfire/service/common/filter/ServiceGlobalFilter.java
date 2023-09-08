package proj.fzy.campfire.service.common.filter;

import cn.hutool.json.JSONUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import proj.fzy.campfire.model.dto.AuthenticationInfo;
import proj.fzy.campfire.service.common.utils.JwtUtils;
import proj.fzy.campfire.service.common.utils.RedisUtils;
import proj.fzy.campfire.service.common.utils.ServiceUtils;

import java.io.IOException;

@Component
public class ServiceGlobalFilter extends OncePerRequestFilter {

    private final RedisUtils redisUtils;
    private final JwtUtils jwtUtils;

    public ServiceGlobalFilter(RedisUtils redisUtils, JwtUtils jwtUtils) {
        this.redisUtils = redisUtils;
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = ServiceUtils.extraTokenFromRequest(request);
        if (token != null && jwtUtils.verify(token)) {
            String key = RedisUtils.getSignedInAccountKey(jwtUtils.getId(token));
            if (redisUtils.exists(key)) {
                AuthenticationInfo authenticationInfo = JSONUtil.toBean(redisUtils.get(key), AuthenticationInfo.class);
                if (authenticationInfo.getToken().equals(token)) {
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                            new UsernamePasswordAuthenticationToken(
                                    authenticationInfo,
                                    null,
                                    authenticationInfo.getRoleNames().stream().map(SimpleGrantedAuthority::new).toList());
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
