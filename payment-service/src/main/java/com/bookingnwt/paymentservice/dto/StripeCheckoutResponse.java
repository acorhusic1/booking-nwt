package com.bookingnwt.paymentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StripeCheckoutResponse {
    private String sessionId;
    private String url;
}
