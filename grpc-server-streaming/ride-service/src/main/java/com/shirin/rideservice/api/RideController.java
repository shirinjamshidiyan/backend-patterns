package com.shirin.rideservice.api;

import com.shirin.rideservice.grpc.client.DriverGrpcClient;
import com.shirin.serverstreaming.grpc.v1.Driver;
import com.shirin.serverstreaming.grpc.v1.DriverLocationUpdate;
import io.grpc.Status;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@RestController
@RequestMapping("/api/rides/driver-locations")
@AllArgsConstructor
@Slf4j
public class RideController {

    private final DriverGrpcClient driverGrpcClient;

    @GetMapping(
            value = "/limited",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public SseEmitter watchLimitedDriverLocationsStream(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam int updates)
    {

        SseEmitter emitter = new SseEmitter(0L);

        AtomicReference<Runnable> cancelGrpcCallReference  = new AtomicReference<>(() -> {});

        AtomicBoolean streamEnded = new AtomicBoolean(false);

        Runnable cancelGrpcCall = driverGrpcClient.watchLimitedDriverLocations(
                latitude,
                longitude,
                updates,

                update -> sendUpdate(emitter, update, streamEnded, cancelGrpcCallReference),

                // gRPC stream ended with an error.
                error -> handleGrpcError( emitter, error, streamEnded),

                //  gRPC stream completed normally.
                () -> completeNormally( emitter, streamEnded)

        );
        cancelGrpcCallReference.set(cancelGrpcCall);

        registerSseLifecycle(emitter, streamEnded, cancelGrpcCall);

        return emitter;
    }

    @GetMapping(
            value = "/unlimited",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public SseEmitter watchUnlimitedDriverLocationsStream(
            @RequestParam double latitude,
            @RequestParam double longitude) {

        SseEmitter emitter = new SseEmitter(0L);

        AtomicBoolean streamEnded = new AtomicBoolean(false);

        AtomicReference<Runnable> cancelGrpcCallReference  = new AtomicReference<>(() -> {});


        Runnable cancelGrpcCall  =
                driverGrpcClient.watchUnlimitedDriverLocations(
                        latitude,
                        longitude,

                        update -> sendUpdate(emitter, update, streamEnded, cancelGrpcCallReference),

                        // gRPC stream ended with an error.
                        error -> handleGrpcError( emitter, error, streamEnded),

                        // gRPC stream completed normally.
                        () -> completeNormally( emitter, streamEnded)

                );

        cancelGrpcCallReference.set(cancelGrpcCall);

        registerSseLifecycle(emitter, streamEnded, cancelGrpcCall);

        return emitter;
    }

    private void sendUpdate(SseEmitter emitter,
                            DriverLocationUpdate update,
                            AtomicBoolean streamEnded,
                            AtomicReference<Runnable> cancelGrpcCallReference) {

        if (streamEnded.get()) {
            return;
        }

        Driver driver = update.getDriver();
        DriverLocationHttpResponse response = new DriverLocationHttpResponse(
                update.getSequence(),
                driver.getId(),
                driver.getName(),
                driver.getStatus().name(),
                driver.getDriverLatitude(),
                driver.getDriverLongitude()
        );

        try {
            emitter.send(
                    SseEmitter.event()
                            .id(String.valueOf(update.getSequence()))
                            .name("driver-location")
                            .data(response)
            );
        } catch (IOException e) {

            // HTTP client is no longer receiving data. => Cancel gRpc call
            if (streamEnded.compareAndSet(false, true)) {

                log.info("SSE client disconnected; cancelling gRPC stream");
                cancelGrpcCallReference.get().run();
                emitter.completeWithError(e);

            }
        }
    }
    private void handleGrpcError(SseEmitter emitter,
                                 Throwable error,
                                 AtomicBoolean streamEnded) {

        if (!streamEnded.compareAndSet(false, true)) {
            return;
        }
        Status status = Status.fromThrowable(error);

        if (status.getCode() == Status.Code.CANCELLED) {

            log.info("gRPC stream was cancelled");
            emitter.complete();

        } else {

            log.error(
                    "gRPC stream failed: status={}, description={}",
                    status.getCode(),
                    status.getDescription(),
                    error
            );

            emitter.completeWithError(error);
        }

    }

    private void completeNormally(SseEmitter emitter, AtomicBoolean streamEnded) {

        if (streamEnded.compareAndSet(false, true)) {

            log.info("gRPC stream completed normally");
            emitter.complete();
        }

    }

    private void registerSseLifecycle(
            SseEmitter emitter,
            AtomicBoolean streamEnded,
            Runnable cancelGrpcCall)
    {
        emitter.onCompletion(() -> {

            if (streamEnded.compareAndSet(false, true)) {

                log.info("SSE connection completed; cancelling gRPC stream");
                cancelGrpcCall.run();
            }
        });
        emitter.onTimeout(() -> {

            if (streamEnded.compareAndSet(false, true)) {

                log.info("SSE connection timed out; cancelling gRPC stream");
                cancelGrpcCall.run();
                emitter.complete();
            }
        });
        emitter.onError(error -> {

            if (streamEnded.compareAndSet(false, true)) {

                log.info(" SSE connection failed; cancelling gRPC stream: {}",  error.getMessage());
                cancelGrpcCall.run();
            }
        });



    }

}
