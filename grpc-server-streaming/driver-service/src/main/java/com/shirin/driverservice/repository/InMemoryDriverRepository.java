package com.shirin.driverservice.repository;

import com.shirin.serverstreaming.grpc.v1.Driver;
import com.shirin.serverstreaming.grpc.v1.DriverStatus;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class InMemoryDriverRepository {

    private final List<Driver> drivers = List.of(
            Driver.newBuilder()
                    .setId(1L)
                    .setName("D1")
                    .setStatus(DriverStatus.DRIVER_STATUS_AVAILABLE)
                    .setDriverLatitude(55.6761)
                    .setDriverLongitude(12.5683)
                    .build(),

            Driver.newBuilder()
                    .setId(2L)
                    .setName("D2")
                    .setStatus(DriverStatus.DRIVER_STATUS_BUSY)
                    .setDriverLatitude(55.6810)
                    .setDriverLongitude(12.5330)
                    .build(),

            Driver.newBuilder()
                    .setId(3L)
                    .setName("D3")
                    .setStatus(DriverStatus.DRIVER_STATUS_AVAILABLE)
                    .setDriverLatitude(55.7060)
                    .setDriverLongitude(12.5770)
                    .build(),

            Driver.newBuilder()
                    .setId(4L)
                    .setName("D4")
                    .setStatus(DriverStatus.DRIVER_STATUS_AVAILABLE)
                    .setDriverLatitude(55.7260)
                    .setDriverLongitude(12.5780)
                    .build()
    );

    public List<Driver> findAvailableDrivers() {
        return drivers.stream()
                .filter(driver -> driver.getStatus() == DriverStatus.DRIVER_STATUS_AVAILABLE)
                .toList();
    }
}
