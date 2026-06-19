package com.shirin.rideservice.config;

import com.shirin.unaryservicecall.grpc.v1.DriverServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcClientConfig {

    @Bean(destroyMethod = "shutdown")
    public ManagedChannel managedChannel(
           @Value("${grpc.driverservice.address}") String address,
           @Value("${grpc.driverservice.port}") int grpcPort
    )
    {
       return ManagedChannelBuilder
                .forAddress(address, grpcPort)
                .usePlaintext()
                .build();
    }

    @Bean
    public DriverServiceGrpc.DriverServiceBlockingStub blockingStub(ManagedChannel channel)
    {
        return DriverServiceGrpc.newBlockingStub(channel);
    }

}
