package com.dlywlotus.echo_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class EchoBackendApplication {
    //TODO: Integration tests
    // 1. Edge case where user disconnects while the cron job is running
    // 2. Test if the user A receives a message sent by user B
    // 3. Test if system can handle user spamming join room button (assuming it is not handled on FE)

    public static void main(String[] args) {
        SpringApplication.run(EchoBackendApplication.class, args);
    }
}
