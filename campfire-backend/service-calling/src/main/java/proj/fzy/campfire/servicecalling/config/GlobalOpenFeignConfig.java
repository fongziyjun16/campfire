package proj.fzy.campfire.servicecalling.config;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Configuration
public class GlobalOpenFeignConfig {
    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            String token = null;
            if (requestAttributes != null && (token = requestAttributes.getRequest().getHeader(HttpHeaders.AUTHORIZATION)) != null) {
                Map<String, Collection<String>> headers = requestTemplate.headers();
                headers.put(HttpHeaders.AUTHORIZATION, List.of(token));
                headers.put("inner-service-calling", List.of("true"));
                requestTemplate.headers(headers);
            }
        };
    }
}
