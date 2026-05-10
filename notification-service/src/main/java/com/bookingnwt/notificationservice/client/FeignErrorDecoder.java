package com.bookingnwt.notificationservice.client;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * Task 5 - Translate 4xx/5xx HTTP odgovore iz downstream servisa u smislene exceptione.
 *
 * NAPOMENA: 4xx greske su tretirane kao FeignClientBadRequestException → NE okida
 * circuit breaker (kriva je callerova request), 5xx kao FeignServerException
 * (okida fallback i circuit breaker).
 */
@Slf4j
@Configuration
public class FeignErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        int status = response.status();

        if (status >= 400 && status < 500) {
            log.warn("[Feign] Client error {} pri pozivu {}", status, methodKey);
            return new FeignClientBadRequestException(
                    "Downstream client error: " + status + " — " + methodKey);
        }
        if (status >= 500) {
            log.error("[Feign] Server error {} pri pozivu {}", status, methodKey);
            return new FeignServerException(
                    "Downstream server error: " + status + " — " + methodKey);
        }
        return defaultDecoder.decode(methodKey, response);
    }

    public static class FeignClientBadRequestException extends RuntimeException {
        public FeignClientBadRequestException(String message) { super(message); }
    }

    public static class FeignServerException extends RuntimeException {
        public FeignServerException(String message) { super(message); }
    }
}
