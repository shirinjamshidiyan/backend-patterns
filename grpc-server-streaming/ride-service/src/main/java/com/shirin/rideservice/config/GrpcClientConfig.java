package com.shirin.rideservice.config;

import com.shirin.serverstreaming.grpc.v1.DriverServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcClientConfig {

    @Bean(destroyMethod = "shutdown")
    public ManagedChannel driverServiceChannel(
            @Value("${grpc.driverservice.address}") String address,
            @Value("${grpc.driverservice.port}") int port
    )
    {
        return ManagedChannelBuilder
                .forAddress(address, port)
                .usePlaintext()
                .build();
    }

    @Bean
   public DriverServiceGrpc.DriverServiceStub driverServiceAsyncStub(ManagedChannel channel)
    {
        //suitable for receiving streaming message through callbacks
       return DriverServiceGrpc.newStub(channel);

    }
}
