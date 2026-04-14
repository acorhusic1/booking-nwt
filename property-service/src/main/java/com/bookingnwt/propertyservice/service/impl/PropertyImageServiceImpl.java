package com.bookingnwt.propertyservice.service.impl;

import com.bookingnwt.propertyservice.dto.PropertyImageRequest;
import com.bookingnwt.propertyservice.dto.PropertyImageResponse;
import com.bookingnwt.propertyservice.exception.ResourceNotFoundException;
import com.bookingnwt.propertyservice.mapper.PropertyImageMapper;
import com.bookingnwt.propertyservice.model.Property;
import com.bookingnwt.propertyservice.model.PropertyImage;
import com.bookingnwt.propertyservice.repository.PropertyImageRepository;
import com.bookingnwt.propertyservice.repository.PropertyRepository;
import com.bookingnwt.propertyservice.service.PropertyImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PropertyImageServiceImpl implements PropertyImageService {

    private final PropertyImageRepository imageRepository;
    private final PropertyRepository propertyRepository;
    private final PropertyImageMapper imageMapper;

    @Override
    @Transactional(readOnly = true)
    public List<PropertyImageResponse> getImagesByPropertyId(Long propertyId) {
        return imageRepository.findByPropertyId(propertyId)
                .stream()
                .map(imageMapper::toResponse)
                .toList();
    }

    @Override
    public PropertyImageResponse addImage(Long propertyId, PropertyImageRequest request) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Nekretnina sa ID " + propertyId + " nije pronađena"));
        PropertyImage image = imageMapper.toEntity(request);
        image.setProperty(property);
        PropertyImage saved = imageRepository.save(image);
        return imageMapper.toResponse(saved);
    }

    @Override
    public void deleteImage(Long imageId) {
        if (!imageRepository.existsById(imageId)) {
            throw new ResourceNotFoundException("Slika sa ID " + imageId + " nije pronađena");
        }
        imageRepository.deleteById(imageId);
    }
}
