package com.bookingnwt.propertyservice.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Propagira Authorization header iz incoming HTTP request-a u sve outgoing Feign pozive.
 *
 * Bez ovog: poziv iz property-service na user-service nema JWT token → user-service
 * vraca 401 → F16 verifikacija enforce ne radi (Feign exception → fallback dozvoljava
 * kreiranje neverifikovanim hostovima).
 */
@Configuration
public class FeignAuthInterceptor {

    @Bean
    public RequestInterceptor authForwardingInterceptor() {
        return (RequestTemplate template) -> {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) return;
            HttpServletRequest request = attrs.getRequest();
            String auth = request.getHeader("Authorization");
            if (auth != null && !auth.isBlank()) {
                template.header("Authorization", auth);
            }
        };
    }
}
