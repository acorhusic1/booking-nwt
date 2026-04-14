package com.bookingnwt.propertyservice.controller;

import com.bookingnwt.propertyservice.dto.WishlistItemRequest;
import com.bookingnwt.propertyservice.dto.WishlistItemResponse;
import com.bookingnwt.propertyservice.dto.WishlistRequest;
import com.bookingnwt.propertyservice.dto.WishlistResponse;
import com.bookingnwt.propertyservice.exception.GlobalExceptionHandler;
import com.bookingnwt.propertyservice.exception.ResourceNotFoundException;
import com.bookingnwt.propertyservice.service.WishlistService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WishlistController.class)
@Import(GlobalExceptionHandler.class)
class WishlistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WishlistService wishlistService;

    private WishlistResponse createWishlistResponse() {
        WishlistResponse r = new WishlistResponse();
        r.setId(1L);
        r.setGuestId(1L);
        r.setName("Omiljeni smještaji");
        r.setCreatedAt(LocalDateTime.now());
        return r;
    }

    private WishlistItemResponse createItemResponse() {
        WishlistItemResponse r = new WishlistItemResponse();
        r.setId(1L);
        r.setWishlistId(1L);
        r.setPropertyId(10L);
        r.setAddedAt(LocalDateTime.now());
        return r;
    }

    @Test
    void getWishlistsByGuestId_shouldReturn200() throws Exception {
        when(wishlistService.getWishlistsByGuestId(1L)).thenReturn(List.of(createWishlistResponse()));

        mockMvc.perform(get("/api/wishlists/guest/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Omiljeni smještaji"));
    }

    @Test
    void getWishlistById_shouldReturn200() throws Exception {
        when(wishlistService.getWishlistById(1L)).thenReturn(createWishlistResponse());

        mockMvc.perform(get("/api/wishlists/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.guestId").value(1));
    }

    @Test
    void getWishlistById_shouldReturn404_whenNotFound() throws Exception {
        when(wishlistService.getWishlistById(99L)).thenThrow(new ResourceNotFoundException("Nije pronađena"));

        mockMvc.perform(get("/api/wishlists/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createWishlist_shouldReturn201_whenValid() throws Exception {
        WishlistRequest request = new WishlistRequest();
        request.setGuestId(1L);
        request.setName("Nova lista");

        when(wishlistService.createWishlist(any())).thenReturn(createWishlistResponse());

        mockMvc.perform(post("/api/wishlists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void createWishlist_shouldReturn400_whenInvalid() throws Exception {
        WishlistRequest request = new WishlistRequest();
        // Missing required fields

        mockMvc.perform(post("/api/wishlists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteWishlist_shouldReturn204() throws Exception {
        doNothing().when(wishlistService).deleteWishlist(1L);

        mockMvc.perform(delete("/api/wishlists/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void getItems_shouldReturn200() throws Exception {
        when(wishlistService.getItemsByWishlistId(1L)).thenReturn(List.of(createItemResponse()));

        mockMvc.perform(get("/api/wishlists/1/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].propertyId").value(10));
    }

    @Test
    void addItem_shouldReturn201() throws Exception {
        WishlistItemRequest request = new WishlistItemRequest();
        request.setPropertyId(10L);

        when(wishlistService.addItem(eq(1L), any())).thenReturn(createItemResponse());

        mockMvc.perform(post("/api/wishlists/1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.propertyId").value(10));
    }

    @Test
    void removeItem_shouldReturn204() throws Exception {
        doNothing().when(wishlistService).removeItem(5L);

        mockMvc.perform(delete("/api/wishlists/1/items/5"))
                .andExpect(status().isNoContent());
    }
}
