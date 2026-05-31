package com.bookingnwt.propertyservice.client;

import com.bookingnwt.propertyservice.dto.UserDTO;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

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

    @Override
    public List<Map<String, Object>> getVerifications(Long userId) {
        // Ako user-service ne odgovara, ne mozemo potvrditi verifikaciju —
        // bolje da je host blokiran nego da ide neverifikovan kreiranje.
        return Collections.emptyList();
    }
}
