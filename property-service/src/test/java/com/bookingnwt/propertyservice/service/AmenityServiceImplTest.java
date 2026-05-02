package com.bookingnwt.propertyservice.service;

import com.bookingnwt.propertyservice.dto.AmenityRequest;
import com.bookingnwt.propertyservice.dto.AmenityResponse;
import com.bookingnwt.propertyservice.exception.ResourceNotFoundException;
import com.bookingnwt.propertyservice.mapper.AmenityMapper;
import com.bookingnwt.propertyservice.model.Amenity;
import com.bookingnwt.propertyservice.model.AmenityCategory;
import com.bookingnwt.propertyservice.repository.AmenityRepository;
import com.bookingnwt.propertyservice.service.impl.AmenityServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AmenityServiceImplTest {

    @Mock
    private AmenityRepository amenityRepository;

    @Mock
    private AmenityMapper amenityMapper;

    @InjectMocks
    private AmenityServiceImpl amenityService;

    private Amenity amenity;
    private AmenityRequest request;
    private AmenityResponse response;

    @BeforeEach
    void setUp() {
        amenity = new Amenity();
        amenity.setId(1L);
        amenity.setName("WiFi");
        amenity.setCategory(AmenityCategory.BASIC);

        request = new AmenityRequest();
        request.setName("WiFi");
        request.setCategory("BASIC");

        response = new AmenityResponse();
        response.setId(1L);
        response.setName("WiFi");
        response.setCategory("BASIC");
    }

    @Test
    void getAllAmenities_shouldReturnList() {
        when(amenityRepository.findAll()).thenReturn(List.of(amenity));
        when(amenityMapper.toResponse(amenity)).thenReturn(response);

        List<AmenityResponse> result = amenityService.getAllAmenities();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("WiFi");
    }

    @Test
    void getAmenityById_shouldReturnAmenity_whenExists() {
        when(amenityRepository.findById(1L)).thenReturn(Optional.of(amenity));
        when(amenityMapper.toResponse(amenity)).thenReturn(response);

        AmenityResponse result = amenityService.getAmenityById(1L);

        assertThat(result.getName()).isEqualTo("WiFi");
    }

    @Test
    void getAmenityById_shouldThrow_whenNotFound() {
        when(amenityRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> amenityService.getAmenityById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createAmenity_shouldReturnCreated() {
        when(amenityMapper.toEntity(request)).thenReturn(amenity);
        when(amenityRepository.save(amenity)).thenReturn(amenity);
        when(amenityMapper.toResponse(amenity)).thenReturn(response);

        AmenityResponse result = amenityService.createAmenity(request);

        assertThat(result.getName()).isEqualTo("WiFi");
        verify(amenityRepository).save(amenity);
    }
}
