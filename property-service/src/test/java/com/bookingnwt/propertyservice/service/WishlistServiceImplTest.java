package com.bookingnwt.propertyservice.service;

import com.bookingnwt.propertyservice.dto.WishlistItemRequest;
import com.bookingnwt.propertyservice.dto.WishlistItemResponse;
import com.bookingnwt.propertyservice.dto.WishlistRequest;
import com.bookingnwt.propertyservice.dto.WishlistResponse;
import com.bookingnwt.propertyservice.exception.ResourceNotFoundException;
import com.bookingnwt.propertyservice.mapper.WishlistMapper;
import com.bookingnwt.propertyservice.model.Wishlist;
import com.bookingnwt.propertyservice.model.WishlistItem;
import com.bookingnwt.propertyservice.repository.WishlistItemRepository;
import com.bookingnwt.propertyservice.repository.WishlistRepository;
import com.bookingnwt.propertyservice.service.impl.WishlistServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WishlistServiceImplTest {

    @Mock
    private WishlistRepository wishlistRepository;

    @Mock
    private WishlistItemRepository wishlistItemRepository;

    @Mock
    private WishlistMapper wishlistMapper;

    @InjectMocks
    private WishlistServiceImpl wishlistService;

    private Wishlist wishlist;
    private WishlistItem item;
    private WishlistRequest wishlistRequest;
    private WishlistResponse wishlistResponse;
    private WishlistItemRequest itemRequest;
    private WishlistItemResponse itemResponse;

    @BeforeEach
    void setUp() {
        wishlist = new Wishlist(1L, "Omiljeni smještaji");
        wishlist.setId(1L);

        item = new WishlistItem();
        item.setId(1L);
        item.setWishlist(wishlist);
        item.setPropertyId(10L);
        item.setAddedAt(LocalDateTime.now());

        wishlistRequest = new WishlistRequest();
        wishlistRequest.setGuestId(1L);
        wishlistRequest.setName("Omiljeni smještaji");

        wishlistResponse = new WishlistResponse();
        wishlistResponse.setId(1L);
        wishlistResponse.setGuestId(1L);
        wishlistResponse.setName("Omiljeni smještaji");

        itemRequest = new WishlistItemRequest();
        itemRequest.setPropertyId(10L);

        itemResponse = new WishlistItemResponse();
        itemResponse.setId(1L);
        itemResponse.setWishlistId(1L);
        itemResponse.setPropertyId(10L);
    }

    @Test
    void getWishlistsByGuestId_shouldReturnList() {
        when(wishlistRepository.findByGuestId(1L)).thenReturn(List.of(wishlist));
        when(wishlistMapper.toResponse(wishlist)).thenReturn(wishlistResponse);

        List<WishlistResponse> result = wishlistService.getWishlistsByGuestId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Omiljeni smještaji");
    }

    @Test
    void getWishlistById_shouldReturnWishlist_whenExists() {
        when(wishlistRepository.findById(1L)).thenReturn(Optional.of(wishlist));
        when(wishlistMapper.toResponse(wishlist)).thenReturn(wishlistResponse);

        WishlistResponse result = wishlistService.getWishlistById(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getWishlistById_shouldThrow_whenNotFound() {
        when(wishlistRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> wishlistService.getWishlistById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createWishlist_shouldReturnCreated() {
        when(wishlistMapper.toEntity(wishlistRequest)).thenReturn(wishlist);
        when(wishlistRepository.save(wishlist)).thenReturn(wishlist);
        when(wishlistMapper.toResponse(wishlist)).thenReturn(wishlistResponse);

        WishlistResponse result = wishlistService.createWishlist(wishlistRequest);

        assertThat(result.getName()).isEqualTo("Omiljeni smještaji");
        verify(wishlistRepository).save(wishlist);
    }

    @Test
    void deleteWishlist_shouldSucceed_whenExists() {
        when(wishlistRepository.existsById(1L)).thenReturn(true);

        wishlistService.deleteWishlist(1L);

        verify(wishlistRepository).deleteById(1L);
    }

    @Test
    void deleteWishlist_shouldThrow_whenNotFound() {
        when(wishlistRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> wishlistService.deleteWishlist(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getItemsByWishlistId_shouldReturnList() {
        when(wishlistItemRepository.findByWishlistId(1L)).thenReturn(List.of(item));
        when(wishlistMapper.toItemResponse(item)).thenReturn(itemResponse);

        List<WishlistItemResponse> result = wishlistService.getItemsByWishlistId(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    void addItem_shouldReturnCreated() {
        WishlistItem newItem = new WishlistItem();
        when(wishlistRepository.findById(1L)).thenReturn(Optional.of(wishlist));
        when(wishlistMapper.toItemEntity(itemRequest)).thenReturn(newItem);
        when(wishlistItemRepository.save(newItem)).thenReturn(item);
        when(wishlistMapper.toItemResponse(item)).thenReturn(itemResponse);

        WishlistItemResponse result = wishlistService.addItem(1L, itemRequest);

        assertThat(result.getPropertyId()).isEqualTo(10L);
    }

    @Test
    void addItem_shouldThrow_whenWishlistNotFound() {
        when(wishlistRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> wishlistService.addItem(99L, itemRequest))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void removeItem_shouldSucceed_whenExists() {
        when(wishlistItemRepository.existsById(1L)).thenReturn(true);

        wishlistService.removeItem(1L);

        verify(wishlistItemRepository).deleteById(1L);
    }

    @Test
    void removeItem_shouldThrow_whenNotFound() {
        when(wishlistItemRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> wishlistService.removeItem(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
