package com.bookingnwt.propertyservice.client;

import com.bookingnwt.propertyservice.dto.UserDTO;
import org.springframework.stereotype.Component;

@Component
public class UserClientFallback implements UserClient {

    @Override
    public UserDTO getUserById(Long id) {
        UserDTO fallback = new UserDTO();
        fallback.setId(id);
        fallback.setFirstName("Korisnik");
        fallback.setLastName("Nedostupan");
        fallback.setEmail("n/a");
        return fallback;
    }
}
