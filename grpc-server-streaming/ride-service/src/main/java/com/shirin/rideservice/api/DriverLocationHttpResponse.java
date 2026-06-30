package com.shirin.rideservice.api;

public record DriverLocationHttpResponse(
        int sequence,
        long driverId,
        String driverName,
        String status,
        double latitude,
        double longitude
) {
}
