package com.shirin.rideservice.api;

import com.shirin.rideservice.grpc.client.DriverGrpcClient;
import com.shirin.rideservice.grpc.client.MatchedDriver;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rides")
@AllArgsConstructor
public class RideController {

    private final DriverGrpcClient grpcClient;


    @PostMapping("/find-driver")
    public ResponseEntity<DriverHttpResponse> findNearestDriver(
            @RequestBody FindNearestDriverHttpRequest request){

        MatchedDriver driver = grpcClient.findNearestDriver(
                request.latitude(),
                request.longitude());

       return ResponseEntity.ok(
                new DriverHttpResponse(
                        driver.id(),
                        driver.name(),
                        driver.status(),
                        driver.latitude(),
                        driver.longitude())
        );

    }

}
