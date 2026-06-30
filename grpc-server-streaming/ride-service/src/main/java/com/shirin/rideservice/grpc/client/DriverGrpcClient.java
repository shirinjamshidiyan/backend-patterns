package com.shirin.rideservice.grpc.client;

import com.shirin.serverstreaming.grpc.v1.DriverLocationUpdate;
import com.shirin.serverstreaming.grpc.v1.DriverServiceGrpc;
import com.shirin.serverstreaming.grpc.v1.DriversLimitedUpdateRequest;
import com.shirin.serverstreaming.grpc.v1.DriversUnlimitedUpdateRequest;
import io.grpc.Status;
import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.ClientResponseObserver;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

@Component
@AllArgsConstructor
@Slf4j
public class DriverGrpcClient {

    private final DriverServiceGrpc.DriverServiceStub asyncStub;

    public Runnable watchLimitedDriverLocations(
            double latitude,
            double longitude,
            int updateCount,
            Consumer<DriverLocationUpdate> onUpdate,
            Consumer<Throwable> onError,
            Runnable onCompleted)
    {
        DriversLimitedUpdateRequest request = DriversLimitedUpdateRequest
                .newBuilder()
                .setPassengerLatitude(latitude)
                .setPassengerLongitude(longitude)
                .setUpdateCount(updateCount)
                .build();

        log.info("Starting limited gRPC stream: latitude={}, longitude={}, updates={}",
                latitude, longitude, updateCount);


        AtomicReference<ClientCallStreamObserver<DriversLimitedUpdateRequest>> callReference =
                new AtomicReference<>();

        asyncStub.watchLimitedDriverLocations(
                request,
                createResponseObserver(
                        "limited",
                        callReference,
                        onUpdate,
                        onError,
                        onCompleted )
        );

        return createCancellationHandle( "limited", callReference );

    }

    public Runnable watchUnlimitedDriverLocations(
            double latitude,
            double longitude,
            Consumer<DriverLocationUpdate> onUpdate,
            Consumer<Throwable> onError,
            Runnable onCompleted)
    {

        DriversUnlimitedUpdateRequest request = DriversUnlimitedUpdateRequest.newBuilder()
                .setPassengerLatitude(latitude)
                .setPassengerLongitude(longitude)
                .build();

        log.info("Starting Unlimited gRPC stream: latitude={}, longitude={}",
                latitude, longitude);

        AtomicReference<ClientCallStreamObserver<DriversUnlimitedUpdateRequest>> callReference
                = new AtomicReference<>();

        asyncStub.watchUnlimitedDriverLocations(
                request,
                createResponseObserver(
                        "unlimited",
                        callReference,
                        onUpdate,
                        onError,
                        onCompleted
                )
        );

        return createCancellationHandle( "unlimited", callReference );

    }


    private <RequestType>
        ClientResponseObserver<RequestType, DriverLocationUpdate>createResponseObserver(
            String streamType,
            AtomicReference<ClientCallStreamObserver<RequestType>> callReference,
            Consumer<DriverLocationUpdate> onUpdate,
            Consumer<Throwable> onError,
            Runnable onCompleted

    )
    {
        return new ClientResponseObserver<>() {

            @Override
            public void beforeStart(ClientCallStreamObserver<RequestType> requestStream) {

                callReference.set(requestStream);
                log.info("{} gRPC stream started", streamType);
            }

            @Override
            public void onNext(DriverLocationUpdate update) {

                log.info( "Received {} location update: sequence={}, driverId={}",
                        streamType, update.getSequence(),  update.getDriver().getId());
                onUpdate.accept(update);
            }

            @Override
            public void onError(Throwable throwable) {

                Status status = Status.fromThrowable(throwable);

                if (status.getCode() == Status.Code.CANCELLED) {
                    log.info("{} gRPC stream cancelled  {}", streamType, status.getDescription());
                } else {
                    log.error("{} gRPC stream failed: status={}, description={}",
                            streamType, status.getCode(), status.getDescription(), throwable);
                }
                onError.accept(throwable);

            }

            @Override
            public void onCompleted() {
                log.info("{} gRPC stream completed by server",streamType);
                onCompleted.run();
            }


        };
    }

    private <RequestType> Runnable createCancellationHandle(
            String streamType,
            AtomicReference<ClientCallStreamObserver<RequestType>> callReference) {

        return () -> {
            ClientCallStreamObserver<RequestType> cancelCall = callReference.get();

            if (cancelCall != null) {
                cancelCall.cancel(streamType + "stream cancelled by Ride Service", null);

                log.info("Ride Service cancelled {} gRPC stream", streamType);
            }
        };

    }
}
