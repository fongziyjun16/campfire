package proj.fzy.campfire.service.common.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.util.WebUtils;
import proj.fzy.campfire.model.dto.AuthenticationInfo;

public class ServiceUtils {

    private static String extraTokenFromBearerAuthorization(HttpServletRequest request) {
        String content = request.getHeader(HttpHeaders.AUTHORIZATION);
        String prefix = "Bearer ";
        if (content != null && content.startsWith(prefix)) {
            return content.substring(prefix.length());
        }
        return null;
    }

    private static String extraTokenFromCookieToken(HttpServletRequest request) {
        Cookie tokenCookie = WebUtils.getCookie(request, "token");
        if (tokenCookie != null) {
            return tokenCookie.getValue();
        }
        return null;
    }

    public static String extraTokenFromRequest(HttpServletRequest request) {
        String token = extraTokenFromBearerAuthorization(request);
        return token != null ? token : extraTokenFromCookieToken(request);
    }

    public static Long calculateOffset(Long pageNo, Long pageSize) {
        return (pageNo - 1) * pageSize;
    }

    public static Long getAccountIdFromSecurityContext() {
        return Long.valueOf(((AuthenticationInfo) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId());
    }

    public static String getUsernameFromSecurityContext() {
        return ((AuthenticationInfo) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
    }

}
