package com.bookingnwt.propertyservice.mapper;

import com.bookingnwt.propertyservice.dto.WishlistRequest;
import com.bookingnwt.propertyservice.dto.WishlistResponse;
import com.bookingnwt.propertyservice.dto.WishlistItemRequest;
import com.bookingnwt.propertyservice.dto.WishlistItemResponse;
import com.bookingnwt.propertyservice.model.Wishlist;
import com.bookingnwt.propertyservice.model.WishlistItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WishlistMapper {

    WishlistResponse toResponse(Wishlist wishlist);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "items", ignore = true)
    Wishlist toEntity(WishlistRequest request);

    @Mapping(target = "wishlistId", source = "wishlist.id")
    WishlistItemResponse toItemResponse(WishlistItem item);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "wishlist", ignore = true)
    @Mapping(target = "addedAt", ignore = true)
    WishlistItem toItemEntity(WishlistItemRequest request);
}
