package com.shirin.rideservice.grpc.client;

public record MatchedDriver (
        long id,
        String name,
        String status,
        double latitude,
        double longitude){

}
