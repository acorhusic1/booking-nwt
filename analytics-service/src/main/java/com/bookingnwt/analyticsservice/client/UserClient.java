package com.bookingnwt.analyticsservice.client;

import com.bookingnwt.analyticsservice.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")
public interface UserClient {
    
    @GetMapping("/api/users/{id}")
    UserDto getUserById(@PathVariable("id") Long id);
}
