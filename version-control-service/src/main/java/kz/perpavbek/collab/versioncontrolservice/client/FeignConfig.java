package kz.perpavbek.collab.versioncontrolservice.client;

import feign.RequestInterceptor;
import kz.perpavbek.collab.versioncontrolservice.security.JwtUtils;
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