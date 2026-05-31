package com.bookingnwt.propertyservice.client;

import com.bookingnwt.propertyservice.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;

@FeignClient(name = "user-service", fallback = UserClientFallback.class)
public interface UserClient {

    @GetMapping("/api/users/{id}")
    UserDTO getUserById(@PathVariable("id") Long id);

    // F16 enforce — provjera da li host ima APPROVED verifikaciju prije
    // kreiranja novog objekta. Vraca listu zahtjeva ({id, status, ...}).
    @GetMapping("/api/users/{userId}/verifications")
    List<Map<String, Object>> getVerifications(@PathVariable("userId") Long userId);
}
