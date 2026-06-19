package com.shirin.rideservice.grpc.client;

import com.shirin.unaryservicecall.grpc.v1.Driver;
import com.shirin.unaryservicecall.grpc.v1.DriverServiceGrpc;
import com.shirin.unaryservicecall.grpc.v1.FindNearestDriverRequest;
import com.shirin.unaryservicecall.grpc.v1.FindNearestDriverResponse;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.concurrent.TimeUnit;

@Component
@AllArgsConstructor
@Slf4j
public class DriverGrpcClient {

    private final DriverServiceGrpc.DriverServiceBlockingStub driverServiceBlockingStub;

    public MatchedDriver findNearestDriver(double latitude, double longitude)
    {
        log.info("Sending FindDriver request: latitude={}, longitude={}", latitude, longitude);

        FindNearestDriverRequest request = FindNearestDriverRequest
                .newBuilder()
                .setPassengerLatitude(latitude)
                .setPassengerLongitude(longitude)
                .build();
        try {
            FindNearestDriverResponse driverResponse = driverServiceBlockingStub
                    .withDeadlineAfter(2, TimeUnit.SECONDS)
                    .findDriver(request);

            Driver driver = driverResponse.getDriver();
            log.info("Received driver: id={}, name={}, status={}", driver.getId(), driver.getName(), driver.getStatus());

            return new MatchedDriver(
                    driver.getId(),
                    driver.getName(),
                    driver.getStatus().name(),
                    driver.getDriverLatitude(),
                    driver.getDriverLongitude()
            );
        }
        catch (StatusRuntimeException exception) {

                Status status = exception.getStatus();
                log.error("FindDriver failed: status={}, description={}", status.getCode(), status.getDescription());
                throw exception;
        }
    }

}
