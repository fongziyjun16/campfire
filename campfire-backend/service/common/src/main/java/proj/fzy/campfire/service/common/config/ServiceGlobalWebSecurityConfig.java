package proj.fzy.campfire.service.common.config;

import cn.hutool.json.JSONUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import proj.fzy.campfire.model.dto.CommonResponse;
import proj.fzy.campfire.service.common.filter.ServiceGlobalFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class ServiceGlobalWebSecurityConfig {

    private final ServiceGlobalFilter serviceGlobalFilter;

    public ServiceGlobalWebSecurityConfig(ServiceGlobalFilter serviceGlobalFilter) {
        this.serviceGlobalFilter = serviceGlobalFilter;
    }

    @Bean
    public GrantedAuthorityDefaults grantedAuthorityDefaults() {
        return new GrantedAuthorityDefaults("");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrfConfigurer -> csrfConfigurer.disable())
                .httpBasic(httpBasicConfigurer -> httpBasicConfigurer.disable())
                .formLogin(formLoginConfigurer -> formLoginConfigurer.disable())
                .logout(logoutConfigurer -> logoutConfigurer.disable())
                .sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(serviceGlobalFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(authorizeHttpRequests ->
                        authorizeHttpRequests.anyRequest().permitAll())
                .exceptionHandling(exceptionHandling ->
                        exceptionHandling
                                .authenticationEntryPoint((request, response, authException) -> {
                                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                                    response.setContentType("application/json");
                                    response.getWriter().write(
                                            JSONUtil.toJsonStr(CommonResponse.simpleResponse(HttpStatus.UNAUTHORIZED.value(), "Authentication Failure")));
                                })
                                .accessDeniedHandler((request, response, accessDeniedException) -> {
                                    response.setStatus(HttpStatus.FORBIDDEN.value());
                                    response.setContentType("application/json");
                                    response.getWriter().write(
                                            JSONUtil.toJsonStr(CommonResponse.simpleResponse(HttpStatus.FORBIDDEN.value(), "Authorization Failure")));
                                }));
        return http.build();
    }

}
