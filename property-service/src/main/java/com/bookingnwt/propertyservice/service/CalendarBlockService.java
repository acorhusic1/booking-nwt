package com.bookingnwt.propertyservice.service;

import com.bookingnwt.propertyservice.dto.CalendarBlockRequest;
import com.bookingnwt.propertyservice.dto.CalendarBlockResponse;

import java.util.List;

public interface CalendarBlockService {
    List<CalendarBlockResponse> getBlocksByPropertyId(Long propertyId);
    CalendarBlockResponse addBlock(Long propertyId, CalendarBlockRequest request);
    void deleteBlock(Long blockId);
}
