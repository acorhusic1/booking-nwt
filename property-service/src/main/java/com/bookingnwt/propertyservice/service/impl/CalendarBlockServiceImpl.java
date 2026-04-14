package com.bookingnwt.propertyservice.service.impl;

import com.bookingnwt.propertyservice.dto.CalendarBlockRequest;
import com.bookingnwt.propertyservice.dto.CalendarBlockResponse;
import com.bookingnwt.propertyservice.exception.ResourceNotFoundException;
import com.bookingnwt.propertyservice.mapper.CalendarBlockMapper;
import com.bookingnwt.propertyservice.model.CalendarBlock;
import com.bookingnwt.propertyservice.model.Property;
import com.bookingnwt.propertyservice.repository.CalendarBlockRepository;
import com.bookingnwt.propertyservice.repository.PropertyRepository;
import com.bookingnwt.propertyservice.service.CalendarBlockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CalendarBlockServiceImpl implements CalendarBlockService {

    private final CalendarBlockRepository calendarBlockRepository;
    private final PropertyRepository propertyRepository;
    private final CalendarBlockMapper calendarBlockMapper;

    @Override
    @Transactional(readOnly = true)
    public List<CalendarBlockResponse> getBlocksByPropertyId(Long propertyId) {
        return calendarBlockRepository.findByPropertyId(propertyId)
                .stream()
                .map(calendarBlockMapper::toResponse)
                .toList();
    }

    @Override
    public CalendarBlockResponse addBlock(Long propertyId, CalendarBlockRequest request) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Nekretnina sa ID " + propertyId + " nije pronađena"));
        CalendarBlock block = calendarBlockMapper.toEntity(request);
        block.setProperty(property);
        CalendarBlock saved = calendarBlockRepository.save(block);
        return calendarBlockMapper.toResponse(saved);
    }

    @Override
    public void deleteBlock(Long blockId) {
        if (!calendarBlockRepository.existsById(blockId)) {
            throw new ResourceNotFoundException("Blok kalendara sa ID " + blockId + " nije pronađen");
        }
        calendarBlockRepository.deleteById(blockId);
    }
}
