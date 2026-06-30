package com.shirin.driverservice.grpc.server;

import com.shirin.driverservice.repository.InMemoryDriverRepository;
import com.shirin.serverstreaming.grpc.v1.*;
import io.grpc.Status;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Component
@Slf4j
@AllArgsConstructor
public class DriverGrpcService extends DriverServiceGrpc.DriverServiceImplBase {

    private final InMemoryDriverRepository repository;
    private final ScheduledExecutorService scheduler;

    @Override
    public void watchLimitedDriverLocations(
            DriversLimitedUpdateRequest request,
            StreamObserver<DriverLocationUpdate> responseObserver) {

        if (request.getUpdateCount() <= 0) {
            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription("update_count must be greater than zero")
                            .asRuntimeException()
            );
            return;
        }

        startLocationStream(
                request.getPassengerLatitude(),
                request.getPassengerLongitude(),
                request.getUpdateCount(),
                responseObserver);

    }

    @Override
    public void watchUnlimitedDriverLocations(
            DriversUnlimitedUpdateRequest request,
            StreamObserver<DriverLocationUpdate> responseObserver) {

        startLocationStream(
                request.getPassengerLatitude(),
                request.getPassengerLongitude(),
                null,
                responseObserver);
    }


    private void startLocationStream(
            double latitude,
            double longitude,
            Integer updateLimit,
            StreamObserver<DriverLocationUpdate> responseObserver
    )
    {
        String streamType = updateLimit == null ? "unlimited" : "limited";

        log.info(
                "Received {} stream request: latitude={}, longitude={}, updateLimit={}",
                streamType,
                latitude,
                longitude,
                updateLimit
        );

        if (latitude < -90 || latitude > 90) {

            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription("Latitude must be between -90 and 90")
                            .asRuntimeException()
            );
            return;
        }

        if (longitude < -180 || longitude > 180) {

            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription("Longitude must be between -180 and 180")
                            .asRuntimeException()
            );
            return;
        }
        List<Driver> availableDrivers = repository.findAvailableDrivers();

        if (availableDrivers.isEmpty()) {
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("No available drivers found")
                            .asRuntimeException()
            );
            return;
        }

        ServerCallStreamObserver<DriverLocationUpdate> serverObserver =
                (ServerCallStreamObserver<DriverLocationUpdate>) responseObserver;

        AtomicInteger sequence = new AtomicInteger(0);
        AtomicBoolean terminated = new AtomicBoolean(false);
        AtomicReference<ScheduledFuture<?>> taskReference = new AtomicReference<>();

        serverObserver.setOnCancelHandler(() -> {

            if (terminated.compareAndSet(false, true)) {

                cancelScheduledTask(taskReference);
                log.info("{} location stream cancelled by client", streamType);
            }
        });

        Runnable sendUpdateTask = () -> {

            if (terminated.get()) {
                return;
            }

            if (serverObserver.isCancelled()) {

                if (terminated.compareAndSet(false, true)) {
                    cancelScheduledTask(taskReference);
                }
                return;
            }

            if (!serverObserver.isReady()) {
                log.debug("Client is not ready for the next location update");
                return;
            }

            try {
                int currentSequence = sequence.incrementAndGet();
                Driver baseDriver = availableDrivers.get((currentSequence - 1) % availableDrivers.size());
                DriverLocationUpdate update = createUpdate(baseDriver, currentSequence);
                serverObserver.onNext(update);

                boolean limitedStreamFinished =
                        updateLimit != null
                                && currentSequence >= updateLimit;

                if (limitedStreamFinished
                        && terminated.compareAndSet(false, true)) {

                    serverObserver.onCompleted();
                    cancelScheduledTask(taskReference);

                    log.info("Completed limited location stream after {} updates", currentSequence);
                }

            } catch (Exception exception) {

                if (terminated.compareAndSet(false, true)) {
                    cancelScheduledTask(taskReference);

                    if (!serverObserver.isCancelled()) {
                        serverObserver.onError(
                                Status.INTERNAL
                                        .withDescription("Failed to produce driver location update")
                                        .withCause(exception)
                                        .asRuntimeException()
                        );
                    }
                }

                log.error("Failed to send {} location update", streamType, exception);
            }
        };

        ScheduledFuture<?> updateTask =
                scheduler.scheduleWithFixedDelay(
                        sendUpdateTask,
                        0,
                        1,
                        TimeUnit.SECONDS
                );

        taskReference.set(updateTask);

        if (terminated.get() || serverObserver.isCancelled()) {
            terminated.set(true);
            updateTask.cancel(false);
        }


    }

    private DriverLocationUpdate createUpdate(Driver base, int sequence) {

        Driver updatedDriver = base.toBuilder()
                .setDriverLatitude(base.getDriverLatitude() + sequence * 0.0001)
                .setDriverLongitude(base.getDriverLongitude() + sequence * 0.0001)
                .build();

        DriverLocationUpdate updated = DriverLocationUpdate.newBuilder()
                .setDriver(updatedDriver)
                .setSequence(sequence)
                .build();

        log.info(
                "Sending location update: sequence={}, driverId={}, latitude={}, longitude={}",
                sequence,
                updatedDriver.getId(),
                updatedDriver.getDriverLatitude(),
                updatedDriver.getDriverLongitude()
        );
        return updated;
    }

    private void cancelScheduledTask(AtomicReference<ScheduledFuture<?>> taskReference) {

        ScheduledFuture<?> task = taskReference.get();

        if (task != null) {
            task.cancel(false);
        }
    }



}
