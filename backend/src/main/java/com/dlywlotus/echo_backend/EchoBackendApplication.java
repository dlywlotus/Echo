package com.dlywlotus.echo_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class EchoBackendApplication {
    //TODO: Integration tests
    // 1. Edge case where user disconnects while the cron job is running
    // 3. Test if system can handle user spamming join room button (assuming it is not handled on FE)

    // Find out purpose of setting roomId for users

    public static void main(String[] args) {
        SpringApplication.run(EchoBackendApplication.class, args);
    }
}
