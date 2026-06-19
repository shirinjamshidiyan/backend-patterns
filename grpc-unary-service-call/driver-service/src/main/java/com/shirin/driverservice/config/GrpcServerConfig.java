package com.shirin.driverservice.config;

import com.shirin.driverservice.grpc.server.DriverGrpcService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.io.IOException;

@Configuration
@Slf4j
public class GrpcServerConfig {

    @Bean(destroyMethod = "shutdown")
    public Server grpcServer(
            DriverGrpcService driverGrpcService,
            @Value("${grpc.server.port}") int grpcPort) throws IOException
    {
        Server server = ServerBuilder
                .forPort(grpcPort)
                .addService(driverGrpcService)
                .build()
                .start();

        log.info("gRPC server started on port {}", grpcPort);

        return server;
    }


    @Bean
    public ApplicationRunner keepGrpcServerRunning(Server grpcServer) {

        return args -> grpcServer.awaitTermination();
    }
}
