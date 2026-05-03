package com.bookingnwt.propertyservice.service;

import com.bookingnwt.propertyservice.dto.PropertyRequest;
import com.bookingnwt.propertyservice.dto.PropertyResponse;
import com.bookingnwt.propertyservice.exception.ResourceNotFoundException;
import com.bookingnwt.propertyservice.mapper.PropertyMapper;
import com.bookingnwt.propertyservice.model.Property;
import com.bookingnwt.propertyservice.repository.PropertyRepository;
import com.bookingnwt.propertyservice.service.impl.PropertyServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PropertyServiceImplTest {

    @Mock
    private PropertyRepository propertyRepository;

    @Mock
    private PropertyMapper propertyMapper;

    @InjectMocks
    private PropertyServiceImpl propertyService;

    private Property property;
    private PropertyRequest request;
    private PropertyResponse response;

    @BeforeEach
    void setUp() {
        property = new Property(1L, "Apartman Centar", "Opis", "Ferhadija 1",
                "Sarajevo", "BiH", new BigDecimal("43.856"), new BigDecimal("18.413"), 4);
        property.setId(1L);

        request = new PropertyRequest();
        request.setHostId(1L);
        request.setName("Apartman Centar");
        request.setDescription("Opis");
        request.setAddress("Ferhadija 1");
        request.setCity("Sarajevo");
        request.setCountry("BiH");

        response = new PropertyResponse();
        response.setId(1L);
        response.setHostId(1L);
        response.setName("Apartman Centar");
        response.setCity("Sarajevo");
        response.setIsActive(true);
        response.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void getAllProperties_shouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 10);
        when(propertyRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(property)));
        when(propertyMapper.toResponse(property)).thenReturn(response);

        Page<PropertyResponse> result = propertyService.getAllProperties(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Apartman Centar");
    }

    @Test
    void getPropertyById_shouldReturnProperty_whenExists() {
        when(propertyRepository.findById(1L)).thenReturn(Optional.of(property));
        when(propertyMapper.toResponse(property)).thenReturn(response);

        PropertyResponse result = propertyService.getPropertyById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Apartman Centar");
    }

    @Test
    void getPropertyById_shouldThrow_whenNotFound() {
        when(propertyRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> propertyService.getPropertyById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getPropertiesByHostId_shouldReturnList() {
        when(propertyRepository.findByHostId(1L)).thenReturn(List.of(property));
        when(propertyMapper.toResponse(property)).thenReturn(response);

        List<PropertyResponse> result = propertyService.getPropertiesByHostId(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    void getPropertiesByCity_shouldReturnList() {
        when(propertyRepository.findByCityAndIsActiveTrue("Sarajevo")).thenReturn(List.of(property));
        when(propertyMapper.toResponse(property)).thenReturn(response);

        List<PropertyResponse> result = propertyService.getPropertiesByCity("Sarajevo");

        assertThat(result).hasSize(1);
    }

    @Test
    void createProperty_shouldReturnCreated() {
        when(propertyMapper.toEntity(request)).thenReturn(property);
        when(propertyRepository.save(property)).thenReturn(property);
        when(propertyMapper.toResponse(property)).thenReturn(response);

        PropertyResponse result = propertyService.createProperty(request);

        assertThat(result.getId()).isEqualTo(1L);
        verify(propertyRepository).save(property);
    }

    @Test
    void updateProperty_shouldReturnUpdated_whenExists() {
        when(propertyRepository.findById(1L)).thenReturn(Optional.of(property));
        when(propertyRepository.save(property)).thenReturn(property);
        when(propertyMapper.toResponse(property)).thenReturn(response);

        PropertyResponse result = propertyService.updateProperty(1L, request);

        assertThat(result.getId()).isEqualTo(1L);
        verify(propertyMapper).updateEntity(request, property);
    }

    @Test
    void updateProperty_shouldThrow_whenNotFound() {
        when(propertyRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> propertyService.updateProperty(99L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteProperty_shouldSucceed_whenExists() {
        when(propertyRepository.existsById(1L)).thenReturn(true);

        propertyService.deleteProperty(1L);

        verify(propertyRepository).deleteById(1L);
    }

    @Test
    void deleteProperty_shouldThrow_whenNotFound() {
        when(propertyRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> propertyService.deleteProperty(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
