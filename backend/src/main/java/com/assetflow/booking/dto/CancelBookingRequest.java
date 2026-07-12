package com.assetflow.booking.dto;

import lombok.Data;

@Data
public class CancelBookingRequest {

    private String cancellationReason;
}
