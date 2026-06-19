package com.shirin.rideservice.api;

public record FindNearestDriverHttpRequest(
        double latitude,
        double longitude
) {
}
