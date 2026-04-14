package com.bookingnwt.propertyservice.mapper;

import com.bookingnwt.propertyservice.dto.ReviewRequest;
import com.bookingnwt.propertyservice.dto.ReviewResponse;
import com.bookingnwt.propertyservice.model.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    ReviewResponse toResponse(Review review);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "overallRating", ignore = true)
    @Mapping(target = "hostReply", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "repliedAt", ignore = true)
    Review toEntity(ReviewRequest request);
}
