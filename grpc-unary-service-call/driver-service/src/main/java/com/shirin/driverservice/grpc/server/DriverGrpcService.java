package com.shirin.driverservice.grpc.server;

import com.shirin.driverservice.repository.InMemoryDriverRepository;
import com.shirin.unaryservicecall.grpc.v1.Driver;
import com.shirin.unaryservicecall.grpc.v1.DriverServiceGrpc;
import com.shirin.unaryservicecall.grpc.v1.FindNearestDriverRequest;
import com.shirin.unaryservicecall.grpc.v1.FindNearestDriverResponse;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class DriverGrpcService extends DriverServiceGrpc.DriverServiceImplBase {

    private final InMemoryDriverRepository repository;

    @Override
    public void findDriver(FindNearestDriverRequest request,
                           StreamObserver<FindNearestDriverResponse> responseObserver) {

        log.info("Received FindNearestDriver request: latitude ={}, longitude={}",
                request.getPassengerLatitude(),
                request.getPassengerLongitude()
        );

        if (request.getPassengerLatitude() < -90 || request.getPassengerLatitude() > 90) {

            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription("Latitude must be between -90 and 90")
                            .asRuntimeException()
            );
            return;
        }
        if (request.getPassengerLongitude() < -180 || request.getPassengerLongitude() > 180) {

            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription("Longitude must be between -180 and 180")
                            .asRuntimeException()
            );

            return;
        }


        Driver driver = repository
                .findNearestAvailable(request.getPassengerLatitude(), request.getPassengerLongitude())
                .orElse(null);

        if (driver == null) {
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("No available driver found")
                            .asRuntimeException()
            );
            return;
        }

        FindNearestDriverResponse nearest = FindNearestDriverResponse
                .newBuilder()
                .setDriver(driver)
                .build();

// for DEADLINE_EXCEEDED test
//        try {
//            Thread.sleep(3_000);
//        } catch (InterruptedException exception) {
//            Thread.currentThread().interrupt();
//
//            responseObserver.onError(
//                    Status.CANCELLED
//                            .withDescription("Driver search was interrupted")
//                            .asRuntimeException()
//            );
//
//            return;
//        }


        responseObserver.onNext(nearest);
        responseObserver.onCompleted();

        log.info("Returned driver: id={}, name={}", driver.getId(), driver.getName());


    }
}
