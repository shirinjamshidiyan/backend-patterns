package com.shirin.rideservice.api;

public record DriverHttpResponse(
        long id,
        String name,
        String status,
        double latitude,
        double longitude
) {
}
