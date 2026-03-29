package kz.perpavbek.collab.documentservice.client;

import feign.RequestInterceptor;
import kz.perpavbek.collab.documentservice.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FeignConfig {
    private final JwtUtils jwtUtils;
    @Bean
    public RequestInterceptor jwtFeignInterceptor() {
        return template -> {
            String token = jwtUtils.getCurrentToken();
            if (token != null) {
                template.header("Authorization", "Bearer " + token);
            }
        };
    }
}