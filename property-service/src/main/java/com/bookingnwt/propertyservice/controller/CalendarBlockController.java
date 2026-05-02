package com.bookingnwt.propertyservice.controller;

import com.bookingnwt.propertyservice.dto.CalendarBlockRequest;
import com.bookingnwt.propertyservice.dto.CalendarBlockResponse;
import com.bookingnwt.propertyservice.service.CalendarBlockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/properties/{propertyId}/calendar-blocks")
@RequiredArgsConstructor
public class CalendarBlockController {

    private final CalendarBlockService calendarBlockService;

    @GetMapping
    public ResponseEntity<List<CalendarBlockResponse>> getBlocks(@PathVariable Long propertyId) {
        return ResponseEntity.ok(calendarBlockService.getBlocksByPropertyId(propertyId));
    }

    @PostMapping
    public ResponseEntity<CalendarBlockResponse> addBlock(@PathVariable Long propertyId,
                                                          @Valid @RequestBody CalendarBlockRequest request) {
        CalendarBlockResponse created = calendarBlockService.addBlock(propertyId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/{blockId}")
    public ResponseEntity<Void> deleteBlock(@PathVariable Long propertyId,
                                            @PathVariable Long blockId) {
        calendarBlockService.deleteBlock(blockId);
        return ResponseEntity.noContent().build();
    }
}
