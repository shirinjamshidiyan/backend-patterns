package com.shirin.outboxdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TransactionalOutbox1Application {

    public static void main(String[] args) {
        SpringApplication.run(TransactionalOutbox1Application.class, args);
    }

}
